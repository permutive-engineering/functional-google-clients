package com.permutive.google.bigquery.rest.job

import cats.effect.Concurrent
import cats.syntax.all._
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.rest.utils.StreamUtils
import com.permutive.google.bigquery.rest.models.Exceptions._
import com.permutive.google.bigquery.rest.models.job.NewTypes._
import com.permutive.google.bigquery.rest.models.job.{PaginationSettings, PollSettings}
import com.permutive.google.bigquery.rest.models.job.results.NewTypes._
import com.permutive.google.bigquery.rest.models.job.results.{
  CompleteDmlJob,
  CompleteSelectJob,
  IncompleteJob,
  QueryJobResults,
  SelectJobMetadata,
}
import fs2.Stream
import cats.data.NonEmptyList
import com.permutive.google.bigquery.rest.models.job.queryparameters.QueryParameter

trait BigQuerySelectJob[F[_]] {

  def createRunSelectQuery(
    jobId: Option[JobId],
    query: Query,
    legacySql: Boolean,
    location: Option[Location],
    paginationSettings: PaginationSettings = PaginationSettings.default,
    pollSettings: PollSettings = PollSettings.default,
    queryParameters: Option[NonEmptyList[QueryParameter]],
  ): F[(SelectJobMetadata, Stream[F, JobResultRow])]

  def createRunSelectQueryStream(
    jobId: Option[JobId],
    query: Query,
    legacySql: Boolean,
    location: Option[Location],
    paginationSettings: PaginationSettings = PaginationSettings.default,
    pollSettings: PollSettings = PollSettings.default,
    queryParameters: Option[NonEmptyList[QueryParameter]] = None,
  ): Stream[F, JobResultRow] =
    StreamUtils.flattenUnrolled(
      createRunSelectQuery(jobId, query, legacySql, location, paginationSettings, pollSettings, queryParameters),
    )

  def getAllSelectQueryJobResults(
    jobId: JobId,
    location: Option[Location],
    pageToken: Option[PageToken],
    paginationSettings: PaginationSettings = PaginationSettings.default,
  ): F[(SelectJobMetadata, Stream[F, JobResultRow])]

  def getAllSelectQueryJobResults(jobId: JobId): F[(SelectJobMetadata, Stream[F, JobResultRow])] =
    getAllSelectQueryJobResults(jobId, None, None)

  def getAllSelectQueryJobResults(jobId: JobId, pageToken: PageToken): F[(SelectJobMetadata, Stream[F, JobResultRow])] =
    getAllSelectQueryJobResults(jobId, None, Some(pageToken))

  def getAllSelectQueryJobResults(
    jobId: JobId,
    maxResultsPerPage: Int,
  ): F[(SelectJobMetadata, Stream[F, JobResultRow])] =
    getAllSelectQueryJobResults(jobId, None, None, PaginationSettings.default.withMaxResultsPerPage(maxResultsPerPage))

}

object BigQuerySelectJob {

  def apply[F[_]: BigQuerySelectJob]: BigQuerySelectJob[F] = implicitly

  def create[F[_]: BigQueryJob: Concurrent]: F[BigQuerySelectJob[F]] =
    Concurrent[F].pure(impl)

  def impl[F[_]: BigQueryJob](implicit F: Concurrent[F]): BigQuerySelectJob[F] =
    new BigQuerySelectJob[F] {

      override def createRunSelectQuery(
        jobId: Option[JobId],
        query: Query,
        legacySql: Boolean,
        location: Option[Location],
        paginationSettings: PaginationSettings = PaginationSettings.default,
        pollSettings: PollSettings = PollSettings.default,
        queryParameters: Option[NonEmptyList[QueryParameter]],
      ): F[(SelectJobMetadata, Stream[F, JobResultRow])] =
        for {
          job <- BigQueryJob[F].createQueryJobPollSuccessful(
            jobId,
            query,
            legacySql,
            location,
            pollSettings,
            queryParameters,
          )
          res <- getAllSelectQueryJobResults(job.id, location, None, paginationSettings)
        } yield res

      override def getAllSelectQueryJobResults(
        jobId: JobId,
        location: Option[Location],
        pageToken: Option[PageToken],
        paginationSettings: PaginationSettings = PaginationSettings.default,
      ): F[(SelectJobMetadata, Stream[F, JobResultRow])] = {
        val fetch: Option[PageToken] => F[(CompleteSelectJob, Option[PageToken])] =
          getSelectJobResults(jobId, location, _, paginationSettings.maxResultsPerPage).fproduct(_.nextPageToken)

        val res: F[(SelectJobMetadata, Stream[F, JobResultRow])] =
          StreamUtils.unrollResults[F, CompleteSelectJob, JobResultRow, SelectJobMetadata, JobResultRow](
            fetch,
            SelectJobMetadata.fromCompleteSelectJob,
            _.rows.map(_.toList).getOrElse(List.empty),
            Right(_),
            paginationSettings.prefetchPages,
          )

        res
      }

      private def getSelectJobResults(
        jobId: JobId,
        location: Option[Location],
        pageToken: Option[PageToken],
        maxResults: Option[Int],
      ): F[CompleteSelectJob] =
        for {
          qryRes    <- BigQueryJob[F].getQueryJobResults(jobId, location, pageToken, maxResults)
          selectRes <- raiseIfNotSelectResults(qryRes, jobId)
        } yield selectRes

      private def raiseIfNotSelectResults(results: QueryJobResults, jobId: JobId): F[CompleteSelectJob] =
        results match {
          case init: CompleteSelectJob => init.pure
          case _: IncompleteJob =>
            InvalidResultForSelectResultsException("IncompleteJob", jobId).raiseError
          case _: CompleteDmlJob =>
            InvalidResultForSelectResultsException("CompleteDmlJob", jobId).raiseError
        }
    }

}
