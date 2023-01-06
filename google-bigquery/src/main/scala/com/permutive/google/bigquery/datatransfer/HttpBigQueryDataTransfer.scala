/*
 * Copyright 2022 Permutive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.permutive.google.bigquery.datatransfer

import cats.data.NonEmptyList
import cats.effect.kernel.{Concurrent, Sync, Temporal}
import cats.syntax.all._
import com.permutive.google.auth.oauth.models._
import com.permutive.google.bigquery.datatransfer.models.Exceptions._
import com.permutive.google.bigquery.datatransfer.models.NewTypes._
import com.permutive.google.bigquery.datatransfer.models._
import com.permutive.google.bigquery.datatransfer.models.api._
import com.permutive.google.bigquery.http.HttpMethods
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models._
import com.permutive.google.bigquery.models.table.Field
import com.permutive.google.bigquery.models.table.NewTypes._
import com.permutive.google.bigquery.utils.Circe.circeEntityEncoderDropNullValues
import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, Request, Uri}
import retry.RetryPolicy

// [Ben - 2019-02-09]
// This is currently set to force a user access token due to limitations in the Google API.
// Scheduling is available using a user account but pre-alpha for a service account (according to Paul Barnes at Google).
// In our testing was found to be non-functional.

sealed abstract class HttpBigQueryDataTransfer[F[_]: HttpMethods](
    projectName: BigQueryProjectName,
    dataTransferBaseUri: Uri,
    location: Location
)(implicit F: Concurrent[F])
    extends BigQueryDataTransfer[F] {
  object Dsl extends Http4sDsl[F] with Http4sClientDsl[F]
  import Dsl._

  implicit private def circeEntityEncoder[T: Encoder]: EntityEncoder[F, T] =
    circeEntityEncoderDropNullValues[F, T]

  // API Documentation: https://cloud.google.com/bigquery/docs/reference/datatransfer/rest/v1/projects.locations.transferConfigs/create

  private[this] val dataTransferUri: Uri =
    dataTransferBaseUri / "projects" / projectName.value / "locations" / location.value

  private[this] val transferConfigsUri: Uri =
    dataTransferUri / "transferConfigs"

  private def transferConfigUri(configId: ConfigId): Uri =
    transferConfigsUri / configId.value

  private def sendAuthorizedRequest[T](
      request: Request[F],
      description: => String
  )(implicit
      ed: EntityDecoder[F, T]
  ): F[T] =
    HttpMethods[F].sendAuthorizedRequest[T](request, description)

  private def sendAuthorizedGet[T](uri: Uri, description: => String)(implicit
      ed: EntityDecoder[F, T]
  ): F[T] =
    HttpMethods[F].sendAuthorizedGet[T](uri, description)

  // WARNING: This will check to see if the query already exists which may be very slow
  // See `findScheduledQueries` for what it will do
  // Google does not check if the query already exists so we should prevent double creation
  override def scheduleQuery(
      displayName: DisplayName,
      query: Query,
      schedule: Schedule,
      destinationDataset: DatasetId,
      destinationTableName: TableId,
      writeDisposition: WriteDisposition,
      partitioningFieldName: Option[Field.Name] = None
  ): F[Unit] =
    scheduleQuery(
      ScheduleQueryRequest(
        displayName,
        query,
        schedule,
        destinationDataset,
        destinationTableName,
        writeDisposition,
        partitioningFieldName
      )
    )

  // WARNING: This will check to see if the query already exists which may be very slow
  // See `findScheduledQueries` for what it will do
  // Google does not check if the query already exists so we should prevent double creation
  override def scheduleQuery(
      scheduleQueryRequest: ScheduleQueryRequest
  ): F[Unit] =
    raiseIfScheduleExists(
      scheduleQueryRequest.displayName,
      scheduleQueryRequest.destinationDataset
    ) >>
      sendUncheckedScheduleRequest(scheduleQueryRequest)

  private def raiseIfScheduleExists(
      displayName: DisplayName,
      destinationDataset: DatasetId
  ): F[Unit] =
    for {
      exists <- findScheduledQueries(displayName, destinationDataset)
      _ <- raiseIfNonEmpty[ScheduledQuery](
        exists,
        sqs =>
          ScheduledQueryExistsException(
            projectName,
            location,
            displayName,
            destinationDataset,
            sqs.map(_.configId)
          )
      )
    } yield ()

  private def sendUncheckedScheduleRequest(
      req: ScheduleQueryRequest
  ): F[Unit] = {
    val requestBody: Json =
      ScheduleQueryRequestApi(
        req.displayName,
        req.destinationDataset,
        ScheduleQueryParamsApi(
          req.query,
          Some(req.destinationTableName),
          Some(req.writeDisposition),
          req.partitioningFieldName
        ),
        req.schedule
      ).asJson

    sendAuthorizedRequest[TransferConfigsResponseApi](
      POST(requestBody, transferConfigsUri),
      "create scheduled query"
    ).void
  }

  // WARNING: This will check to see if any of the queries already exist which may be very slow
  // See `findScheduledQueries` for what it will do
  // Google does not check if the query already exists so we should prevent double creation
  override def scheduleQueries(requests: List[ScheduleQueryRequest]): F[Unit] =
    for {
      _ <- raiseIfDuplicateRequests(requests)
      exists <- getScheduledQueryIdentities.map(_.toSet)
      _ <- raiseIfAnyScheduleExists(requests, exists)
      _ <- requests.traverse(sendUncheckedScheduleRequest).void
    } yield ()

  override def getScheduledQuery(
      configId: ConfigId
  ): F[Option[ScheduledQuery]] =
    sendAuthorizedGet[TransferConfigsResponseApi](
      transferConfigUri(configId),
      s"get a scheduled query with config ID `$configId`"
    ).map {
      case sq: ScheduledQueryResponseApi => ScheduledQuery.fromApi(sq).some
      case _ => None
    }

  // WARNING: This will return _all_ queries. It goes through each page until there is no next page token
  override def getScheduledQueries: F[List[ScheduledQuery]] =
    getConvertScheduledQueries(
      identity,
      "get scheduled queries"
    )

  // WARNING: THIS IS NOT PERFORMANT
  // It fetches _every_ transfer (which could require multiple requests) and manually searches them all for matching entries
  // At time of writing [Ben: 2019-02-12] Google does not expose any method of filtering results in the API (that I can see)
  override def findScheduledQueries(
      displayName: DisplayName,
      destinationDataset: DatasetId
  ): F[List[ScheduledQuery]] = {
    val filter: ScheduledQueryResponseApi => Boolean =
      sq => sq.displayName === displayName && sq.destinationDatasetId === destinationDataset

    getConvertScheduledQueries(
      identity,
      s"get scheduled queries with display name `$displayName` and destination dataset `$destinationDataset`",
      Some(filter)
    )
  }

  override def updateScheduledQuery(
      configId: ConfigId,
      query: Query,
      destinationTableName: Option[TableId],
      writeDisposition: Option[WriteDisposition],
      partitioningFieldName: Option[Field.Name]
  ): F[Unit] = {
    val requestBody = ScheduleQueryPatchApi(
      ScheduleQueryParamsApi(
        query,
        destinationTableName,
        writeDisposition,
        partitioningFieldName
      )
    )
    sendAuthorizedRequest[TransferConfigsResponseApi](
      PATCH(
        requestBody,
        (transferConfigsUri / configId.value)
          .withQueryParam("updateMask", "params")
      ),
      "update scheduled query"
    ).void
  }

  private def getScheduledQueryIdentities: F[List[ScheduledQueryIdentity]] =
    getConvertScheduledQueries(
      ScheduledQueryIdentity.fromScheduledQuery,
      "get scheduled query identities"
    )

  private def raiseIfAnyScheduleExists(
      requests: List[ScheduleQueryRequest],
      schedulesPresent: Set[ScheduledQueryIdentity]
  ): F[Unit] = {
    val requestsIdents =
      requests.map(ScheduledQueryIdentity.fromScheduleQueryRequest).toSet
    val alreadyPresent = requestsIdents.intersect(schedulesPresent).toList

    raiseIfNonEmpty[ScheduledQueryIdentity](
      alreadyPresent,
      ScheduledQueriesExistException(projectName, location, _)
    )
  }

  private def raiseIfDuplicateRequests(
      requests: List[ScheduleQueryRequest]
  ): F[Unit] = {
    val dupes: List[ScheduledQueryIdentity] =
      requests
        .map(ScheduledQueryIdentity.fromScheduleQueryRequest)
        .groupBy(identity)
        .collect {
          case (x, xs) if xs.lengthCompare(1) > 0 => x
        }
        .toList

    raiseIfNonEmpty(dupes, DuplicateScheduledQueryRequestException(_))
  }

  private def raiseIfNonEmpty[T](
      ts: List[T],
      raise: NonEmptyList[T] => Throwable
  ): F[Unit] =
    NonEmptyList.fromList(ts).fold(F.unit)(nel => F.raiseError(raise(nel)))

  private def getConvertScheduledQueries[T](
      convert: ScheduledQuery => T,
      description: => String,
      filter: Option[ScheduledQueryResponseApi => Boolean] = None
  ): F[List[T]] =
    collectAllPages[
      ListTransferConfigsResponseApi,
      ScheduledQueryResponseApi,
      T
    ](
      transferConfigsUri,
      _.extractScheduledQueries,
      api => convert(ScheduledQuery.fromApi(api)),
      description,
      filter
    )

  private def collectAllPages[Api <: PaginatedApi, T, U](
      baseUri: Uri,
      extract: Api => List[T],
      convert: T => U,
      description: => String,
      filter: Option[T => Boolean]
  )(implicit
      ed: EntityDecoder[F, Api]
  ): F[List[U]] =
    collectRemainingPages(baseUri, extract, convert, description, filter)

  private def collectRemainingPages[Api <: PaginatedApi, T, U](
      baseUri: Uri,
      extract: Api => List[T],
      convert: T => U,
      description: => String,
      filter: Option[T => Boolean],
      pageToken: Option[String] = None,
      acc: List[U] = Nil
  )(implicit
      ed: EntityDecoder[F, Api]
  ): F[List[U]] = {
    val uri = pageToken.fold(baseUri)(baseUri.withQueryParam("pageToken", _))

    for {
      res <- sendAuthorizedGet[Api](uri, description)
      newAcc = acc ::: convertAndFilter(extract(res), convert, filter)
      fin <- res.nextPageToken.fold(newAcc.pure[F])(tok =>
        collectRemainingPages(
          baseUri,
          extract,
          convert,
          description,
          filter,
          Some(tok),
          newAcc
        )
      ) // If the token exists it means there are subsequent pages
    } yield fin
  }

  private def convertAndFilter[T, U](
      results: List[T],
      convert: T => U,
      filter: Option[T => Boolean]
  ): List[U] =
    filter.fold(results.map(convert)) { filt =>
      results.collect { case t if filt(t) => convert(t) }
    }

}

object HttpBigQueryDataTransfer {

  private[this] val dataTransferUri =
    Uri.unsafeFromString("https://bigquerydatatransfer.googleapis.com/v1")

  def impl[F[_]: Temporal](
      projectName: BigQueryProjectName,
      tokenF: F[UserAccountAccessToken],
      location: Location,
      client: Client[F],
      retryPolicy: Option[RetryPolicy[F]] = None
  ): BigQueryDataTransfer[F] = {
    implicit val httpMethods: HttpMethods[F] =
      HttpMethods.impl(client, tokenF.widen, retryPolicy)

    impl(projectName, location)
  }

  def impl[F[_]: Concurrent: HttpMethods](
      projectName: BigQueryProjectName,
      location: Location
  ): BigQueryDataTransfer[F] =
    new HttpBigQueryDataTransfer(
      projectName,
      dataTransferUri,
      location
    ) {}

  def create[F[_]: Sync: Temporal](
      projectName: BigQueryProjectName,
      tokenF: F[UserAccountAccessToken],
      location: Location,
      client: Client[F],
      retryPolicy: Option[RetryPolicy[F]] = None
  ): F[BigQueryDataTransfer[F]] =
    Sync[F].pure(
      impl(projectName, tokenF, location, client, retryPolicy)
    )

  def create[F[_]: Sync: Temporal: HttpMethods](
      projectName: BigQueryProjectName,
      location: Location
  ): F[BigQueryDataTransfer[F]] =
    Sync[F].pure(impl(projectName, location))

}
