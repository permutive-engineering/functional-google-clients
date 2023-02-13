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

package com.permutive.google.bigquery.rest.job

import java.time.Instant
import java.util.UUID

import cats.data.NonEmptyList
import cats.effect.kernel.{Async, Sync, Temporal}
import cats.syntax.all._
import com.permutive.google.auth.oauth.models.AccessToken
import com.permutive.google.bigquery.configuration.RetryConfiguration
import com.permutive.google.bigquery.http.HttpMethods
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models._
import com.permutive.google.bigquery.models.table.NewTypes._
import com.permutive.google.bigquery.rest.ApiEndpoints
import com.permutive.google.bigquery.rest.models.Exceptions._
import com.permutive.google.bigquery.rest.models.api._
import com.permutive.google.bigquery.rest.models.api.job._
import com.permutive.google.bigquery.rest.models.job.NewTypes._
import com.permutive.google.bigquery.rest.models.job._
import com.permutive.google.bigquery.rest.models.job.queryparameters.QueryParameter
import com.permutive.google.bigquery.rest.models.job.results.NewTypes._
import com.permutive.google.bigquery.rest.models.job.results.{DryRunQueryJob, QueryJobResults}
import com.permutive.google.bigquery.rest.utils.UriUtils
import com.permutive.google.bigquery.utils.Circe.circeEntityEncoderDropNullValues
import io.circe.Encoder
import io.circe.syntax._
import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, Request, Uri}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration._

sealed abstract class HttpBigQueryJob[F[_]: HttpMethods: Logger] private (
    projectName: BigQueryProjectName,
    restBaseUri: Uri
)(implicit F: Async[F])
    extends BigQueryJob[F] {
  object Dsl extends Http4sDsl[F] with Http4sClientDsl[F]
  import Dsl._

  implicit private def circeEntityEncoder[T: Encoder]: EntityEncoder[F, T] =
    circeEntityEncoderDropNullValues[F, T]

  private[this] val randomJobId: F[JobId] =
    Sync[F].delay(JobId(UUID.randomUUID().toString))

  private def resolveJobId(jobIdO: Option[JobId]): F[JobId] =
    jobIdO.fold(randomJobId)(_.pure[F])

  // API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/
  // And: https://cloud.google.com/bigquery/docs/reference/rest/v2/jobs

  private[this] val projectUri: Uri =
    restBaseUri / "projects" / projectName.value

  private[this] val jobsUri: Uri =
    projectUri / "jobs"

  private def jobsUriPost[T: Encoder](body: T): Request[F] =
    POST(body.asJson, jobsUri)

  private def jobUri(jobId: JobId): Uri =
    jobsUri / jobId.value

  private def jobUriLocation(jobId: JobId, location: Option[Location]): Uri =
    uriWithLocation(jobUri(jobId), location)

  private def uriWithLocation(uri: Uri, location: Option[Location]): Uri =
    location.fold(uri)(l => uri.withQueryParam("location", l.value))

  override def createQueryJob(
      jobId: Option[JobId],
      query: Query,
      legacySql: Boolean = false,
      location: Option[Location] = None,
      queryParameters: Option[NonEmptyList[QueryParameter]] = None
  ): F[Job] = {
    val requestBody: JobId => CreateQueryJobRequestApi =
      jId =>
        createQueryJobBody(
          jId,
          query,
          legacySql,
          location,
          Some(false),
          queryParameters
        )

    for {
      jId <- resolveJobId(jobId)
      body = requestBody(jId)
      _ <- Logger[F].debug(s"Creating basic query job: $body")
      res <-
        HttpMethods[F]
          .sendAuthorizedRequest[QueryJobResponseApi](
            jobsUriPost(body),
            "create basic job"
          )
          .map(Job.fromResponse)
    } yield res
  }

  override def dryRunQuery(
      query: Query,
      legacySql: Boolean = false,
      location: Option[Location] = None,
      queryParameters: Option[NonEmptyList[QueryParameter]] = None
  ): F[DryRunQueryJob] = {
    val requestBody: JobId => CreateQueryJobRequestApi =
      jId =>
        createQueryJobBody(
          jId,
          query,
          legacySql,
          location,
          Some(true),
          queryParameters
        )

    for {
      jId <- randomJobId
      body = requestBody(jId)
      _ <- Logger[F].debug(s"Dry-running basic query job: $body")
      res <-
        HttpMethods[F]
          .sendAuthorizedRequest[DryRunQueryJobResponseApi](
            jobsUriPost(body),
            "dry-run basic job"
          )
          .map(DryRunQueryJob.fromResponse)
    } yield res
  }

  private def createQueryJobBody(
      jobId: JobId,
      query: Query,
      legacySql: Boolean,
      location: Option[Location],
      dryRun: Option[Boolean],
      queryParameters: Option[NonEmptyList[QueryParameter]]
  ): CreateQueryJobRequestApi =
    CreateQueryJobRequestApi(
      JobConfigurationApi.Query(
        JobConfigurationQueryBasicApi(
          query,
          legacySql,
          queryParameters
        ),
        dryRun
      ),
      JobReferenceApi(
        jobId,
        location,
        projectName
      )
    )

  override def createQueryJobPollSuccessful(
      jobId: Option[JobId],
      query: Query,
      legacySql: Boolean = false,
      location: Option[Location] = None,
      pollSettings: PollSettings = PollSettings.default,
      queryParameters: Option[NonEmptyList[QueryParameter]] = None
  ): F[SuccessfulJob] =
    for {
      job <- createQueryJob(jobId, query, legacySql, location, queryParameters)
      successfulJob <- pollUntilSuccessful(
        job.id,
        pollSettings = pollSettings,
        location = location
      )
    } yield successfulJob

  private def pollUntilSuccessful(
      jobId: JobId,
      pollSettings: PollSettings,
      location: Option[Location]
  ): F[SuccessfulJob] =
    for {
      completeJob <- pollJob(jobId, location, pollSettings)
      successfulJob <- raiseIfJobFailed(completeJob)
    } yield successfulJob

  private def raiseIfJobFailed(job: CompleteJob): F[SuccessfulJob] =
    job match {
      case s: SuccessfulJob => F.pure(s)
      case f: FailedJob => F.raiseError(FailedJobException(f))
    }

  override def createQueryWriteTableJob(
      jobId: Option[JobId],
      query: Query,
      destinationDataset: DatasetId,
      destinationTable: TableId,
      writeDisposition: WriteDisposition,
      legacySql: Boolean = false,
      location: Option[Location] = None
  ): F[Job] = {
    val requestBody: JobId => CreateQueryJobRequestApi =
      jId =>
        CreateQueryJobRequestApi(
          JobConfigurationApi.Query(
            JobConfigurationQueryWriteTableApi(
              query,
              writeDisposition,
              TableReferenceApi(
                projectName,
                destinationDataset,
                destinationTable
              ),
              legacySql
            )
          ),
          JobReferenceApi(
            jId,
            location,
            projectName
          )
        )

    for {
      jId <- resolveJobId(jobId)
      body = requestBody(jId)
      _ <- Logger[F].debug(s"Creating write table query job: $body")
      res <-
        HttpMethods[F]
          .sendAuthorizedRequest[QueryJobResponseApi](
            jobsUriPost(body),
            "create write table job"
          )
          .map(Job.fromResponse)
    } yield res
  }

  override def createQueryWriteTableJobPollSuccessful(
      jobId: Option[JobId],
      query: Query,
      destinationDataset: DatasetId,
      destinationTable: TableId,
      writeDisposition: WriteDisposition,
      legacySql: Boolean = false,
      location: Option[Location] = None,
      pollSettings: PollSettings = PollSettings.default
  ): F[SuccessfulJob] =
    for {
      job <- createQueryWriteTableJob(
        jobId,
        query,
        destinationDataset,
        destinationTable,
        writeDisposition,
        legacySql,
        location
      )
      successfulJob <- pollUntilSuccessful(
        job.id,
        pollSettings,
        location = location
      )
    } yield successfulJob

  override def getQueryJobState(
      jobId: JobId,
      location: Option[Location] = None
  ): F[Job] =
    Logger[F].debug(s"Getting status of job $jobId") >>
      HttpMethods[F]
        .sendAuthorizedGet[QueryJobResponseApi](
          jobUriLocation(jobId, location),
          "get job"
        )
        .map(Job.fromResponse)

  override def pollJob(
      jobId: JobId,
      location: Option[Location] = None,
      pollSettings: PollSettings = PollSettings.default
  ): F[CompleteJob] =
    timeNow.flatMap { now =>
      pollJobInternal(
        jobId,
        location,
        pollSettings.delay,
        now.plusMillis(pollSettings.timeout.toMillis)
      )
    }

  private def pollJobInternal(
      id: JobId,
      location: Option[Location],
      pollDelay: FiniteDuration,
      timeoutAt: Instant,
      pollCount: Int = 1
  ): F[CompleteJob] =
    for {
      _ <- Logger[F].debug(s"Polling job $id for status, poll count $pollCount")
      now <- timeNow
      _ <-
        if (now.isAfter(timeoutAt))
          F.raiseError[Unit](TimeoutException(id, pollDelay, pollCount))
        else F.unit
      job <- getQueryJobState(id, location)
      comp <- job match {
        case comp: CompleteJob =>
          Logger[F].debug(
            s"Job $id completed, took $pollCount polls with interval $pollDelay"
          ) >> comp.pure[F]
        case _: IncompleteJob =>
          Temporal[F].sleep(pollDelay) >> pollJobInternal(
            id,
            location,
            pollDelay,
            timeoutAt,
            pollCount + 1
          )
      }
    } yield comp

  private[this] val timeNow: F[Instant] =
    Async[F].monotonic.map(i => Instant.ofEpochMilli(i.toMillis))

  // API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/jobs/getQueryResults

  private[this] val queriesUri: Uri = projectUri / "queries"

  private def getJobResultUri(
      jobId: JobId,
      pageToken: Option[PageToken],
      maxResults: Option[Int],
      location: Option[Location]
  ): Uri = {
    val init = uriWithLocation(queriesUri / jobId.value, location)
    val withPage = UriUtils.uriWithPageToken(init, pageToken)

    UriUtils.uriWithMaxResults(withPage, maxResults)
  }

  override def getQueryJobResults(
      jobId: JobId,
      location: Option[Location] = None,
      pageToken: Option[PageToken] = None,
      maxResults: Option[Int] = None
  ): F[QueryJobResults] =
    Logger[F].debug(
      s"Getting results of job $jobId (page token: $pageToken; max results per page: $maxResults)"
    ) >>
      HttpMethods[F]
        .sendAuthorizedGet[JobQueryResultApi](
          getJobResultUri(jobId, pageToken, maxResults, location),
          "get job results"
        )
        .map(QueryJobResults.fromResponse)
        .widen[Either[Throwable, QueryJobResults]]
        .rethrow

}

object HttpBigQueryJob {

  def create[F[_]: Async](
      projectName: BigQueryProjectName,
      tokenF: F[AccessToken],
      client: Client[F],
      retryConfiguration: Option[RetryConfiguration[F]] = None
  ): F[BigQueryJob[F]] = {
    implicit val httpMethods: HttpMethods[F] =
      HttpMethods.impl(client, tokenF, retryConfiguration)

    create(projectName)
  }

  def create[F[_]: Async: HttpMethods](
      projectName: BigQueryProjectName
  ): F[BigQueryJob[F]] =
    Slf4jLogger.create[F].map { implicit l =>
      new HttpBigQueryJob(projectName, ApiEndpoints.baseRestUri) {}
    }

}
