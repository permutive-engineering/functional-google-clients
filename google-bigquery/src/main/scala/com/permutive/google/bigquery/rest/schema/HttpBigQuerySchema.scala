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

package com.permutive.google.bigquery.rest.schema

import cats.data.NonEmptyList
import cats.effect.kernel.{Async, Concurrent}
import cats.syntax.all._
import com.permutive.google.auth.oauth.models.AccessToken
import com.permutive.google.bigquery.configuration.RetryConfiguration
import com.permutive.google.bigquery.http.HttpMethods
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models.schema.Access
import com.permutive.google.bigquery.models.table.NewTypes._
import com.permutive.google.bigquery.models.table._
import com.permutive.google.bigquery.rest.ApiEndpoints
import com.permutive.google.bigquery.rest.models.api.schema._
import com.permutive.google.bigquery.rest.models.api.{SchemaApi, TableReferenceApi}
import com.permutive.google.bigquery.rest.models.job.PaginationSettings
import com.permutive.google.bigquery.rest.models.job.results.NewTypes.PageToken
import com.permutive.google.bigquery.rest.models.schema.{
  DatasetObject,
  ListTablesAndViewsResult,
  TablesAndViewsMetadata
}
import com.permutive.google.bigquery.rest.utils.{StreamUtils, UriUtils}
import com.permutive.google.bigquery.utils.Circe.circeEntityEncoderDropNullValues
import fs2.Stream
import io.circe.Encoder
import io.circe.syntax._
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, Request, Uri}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

sealed abstract class HttpBigQuerySchema[F[_]: HttpMethods: Logger] private (
    projectName: BigQueryProjectName,
    restBaseUri: Uri
)(implicit F: Concurrent[F])
    extends BigQuerySchema[F] {
  object Dsl extends Http4sDsl[F] with Http4sClientDsl[F]
  import Dsl._

  implicit private def circeEntityEncoder[T: Encoder]: EntityEncoder[F, T] =
    circeEntityEncoderDropNullValues[F, T]

  private[this] val projectUri: Uri =
    restBaseUri / "projects" / projectName.value

  private def datasetsUri(dataset: DatasetId): Uri =
    projectUri / "datasets" / dataset.value

  private def tablesUri(dataset: DatasetId): Uri =
    datasetsUri(dataset) / "tables"

  private def tableUri(dataset: DatasetId, tableId: TableId): Uri =
    tablesUri(dataset) / tableId.value

  private def tablesUriPost[T: Encoder](
      dataset: DatasetId,
      body: T
  ): Request[F] =
    POST(body.asJson, tablesUri(dataset))

  private def tablesUriPatch[T: Encoder](
      dataset: DatasetId,
      table: TableId,
      body: T
  ): Request[F] =
    PATCH(body.asJson, tableUri(dataset, table))

  private def datasetPost(
      datasetId: DatasetId,
      requestBody: CreateDatasetRequestApi
  ) =
    Logger[F].debug(s"Creating dataset $datasetId: $requestBody") >>
      HttpMethods[F]
        .sendAuthorizedRequest[CreateDatasetResponseApi](
          POST(requestBody.asJson, projectUri / "datasets"),
          "create dataset"
        )
        .void

  override def createDataset(
      datasetId: DatasetId,
      location: Location,
      labels: Option[Map[String, String]]
  ): F[Unit] = {
    val requestBody = CreateDatasetRequestApi(
      DatasetReferenceApi(
        datasetId,
        projectName
      ),
      location,
      None,
      labels
    )

    datasetPost(datasetId, requestBody)
  }

  override def createDataset(
      datasetId: DatasetId,
      location: Location,
      access: NonEmptyList[Access],
      labels: Option[Map[String, String]]
  ): F[Unit] = {
    val requestBody = CreateDatasetRequestApi(
      DatasetReferenceApi(
        datasetId,
        projectName
      ),
      location,
      Some(access),
      labels
    )

    datasetPost(datasetId, requestBody)
  }

  override def createTable(
      name: TableId,
      dataset: DatasetId,
      fields: NonEmptyList[Field],
      partitioning: Option[Partitioning]
  ): F[Unit] = {
    val requestBody: TableRequestApi =
      TableRequestApi(
        TableReferenceApi(
          projectName,
          dataset,
          name
        ),
        SchemaApi(fields),
        partitioning
      )

    assertPartitioningValid(fields, partitioning) >>
      Logger[F].debug(s"Creating table $name: $requestBody") >>
      HttpMethods[F]
        .sendAuthorizedRequest[TableResponseApi](
          tablesUriPost(dataset, requestBody),
          "create table"
        )
        .void
  }

  override def patchTable(
      tableId: TableId,
      datasetId: DatasetId,
      fields: NonEmptyList[Field]
  ): F[Unit] = {
    // Note that although we build a complete `TableRequestApi` here, a PATCH request only applies changes
    // for the fields in the request body (the `schema` property).
    // This is in contrast to the PUT method (not implemented here) which would attempt to update the entire Table resource.
    // see https://cloud.google.com/bigquery/docs/reference/rest/v2/tables/patch
    val requestBody: TableRequestApi =
      TableRequestApi(
        TableReferenceApi(
          projectName,
          datasetId,
          tableId
        ),
        SchemaApi(fields),
        timePartitioning = none // this change will be ignored (see comment above)
      )

    Logger[F].debug(s"Updating fields in table $tableId: $requestBody") >>
      HttpMethods[F]
        .sendAuthorizedRequest[TableResponseApi](
          tablesUriPatch(datasetId, tableId, requestBody),
          "update table"
        )
        .void
  }

  override def createView(
      name: TableId,
      dataset: DatasetId,
      query: Query,
      legacySql: Boolean = false
  ): F[Unit] = {
    val requestBody: CreateViewRequestApi =
      CreateViewRequestApi(
        TableReferenceApi(
          projectName,
          dataset,
          name
        ),
        ViewApi(
          query,
          legacySql
        )
      )

    Logger[F].debug(s"Creating view $name: $requestBody") >>
      HttpMethods[F]
        .sendAuthorizedRequest[TableResponseApi](
          tablesUriPost(dataset, requestBody),
          "create view"
        )
        .void
  }

  override def listTablesAndViews(
      dataset: DatasetId,
      maxResults: Option[Int] = None,
      pageToken: Option[PageToken] = None
  ): F[ListTablesAndViewsResult] = {
    val baseUri = tablesUri(dataset)
    val uri = UriUtils.uriWithPageToken(
      UriUtils.uriWithMaxResults(baseUri, maxResults),
      pageToken
    )

    for {
      _ <- Logger[F].debug(
        s"Retrieving tables in `$dataset`. Max results: $maxResults; Page token: $pageToken"
      )
      raw <- HttpMethods[F].sendAuthorizedGet[ListTablesResponseApi](
        uri,
        "list tables"
      )
      converted <- raw.tables.toList.flatten.traverse(table => F.fromEither(DatasetObject.fromResponse(table)))
    } yield ListTablesAndViewsResult(
      converted,
      raw.totalItems,
      raw.nextPageToken
    )
  }

  override def listAllTablesAndViews(
      dataset: DatasetId,
      paginationSettings: PaginationSettings = PaginationSettings.default
  ): F[(TablesAndViewsMetadata, Stream[F, DatasetObject])] = {
    val fetch: Option[PageToken] => F[
      (ListTablesAndViewsResult, Option[PageToken])
    ] =
      listTablesAndViews(dataset, paginationSettings.maxResultsPerPage, _)
        .fproduct(_.nextPageToken)

    val res: F[(TablesAndViewsMetadata, Stream[F, DatasetObject])] =
      StreamUtils.unrollResults[
        F,
        ListTablesAndViewsResult,
        DatasetObject,
        TablesAndViewsMetadata,
        DatasetObject
      ](
        fetch,
        i => TablesAndViewsMetadata(i.totalObjects),
        _.objects,
        Right(_),
        paginationSettings.prefetchPages
      )

    Logger[F].debug(
      s"Retrieving all tables in `$dataset`. Pagination: $paginationSettings"
    ) >>
      res
  }

  // Need to ensure that the partitioning field provided is in the fields
  // I'm sure this would error on Google's end but makes sense to check here
  private def assertPartitioningValid(
      fields: NonEmptyList[Field],
      partitioning: Option[Partitioning]
  ): F[Unit] = {
    val fieldSpecified: Option[Field.Name] =
      for {
        part <- partitioning
        field <- part.field
      } yield field

    fieldSpecified.fold(F.unit) { field =>
      if (fields.exists(_.name === field))
        F.unit
      else
        F.raiseError(
          new IllegalArgumentException(
            s"Specified partitioning field was not found in table fields when creating a BigQuery table. Partitioning field: $field; Table fields: ${fields.toList}"
          )
        )
    }
  }

}

object HttpBigQuerySchema {

  def create[F[_]: Async](
      projectName: BigQueryProjectName,
      tokenF: F[AccessToken],
      client: Client[F],
      retryConfiguration: Option[RetryConfiguration] = None
  ): F[BigQuerySchema[F]] = {
    implicit val httpMethods: HttpMethods[F] =
      HttpMethods.impl(client, tokenF, retryConfiguration)

    create(projectName)
  }

  def create[F[_]: Async: HttpMethods](
      projectName: BigQueryProjectName
  ): F[BigQuerySchema[F]] =
    Slf4jLogger.create[F].map { implicit l =>
      new HttpBigQuerySchema(projectName, ApiEndpoints.baseRestUri) {}
    }

}
