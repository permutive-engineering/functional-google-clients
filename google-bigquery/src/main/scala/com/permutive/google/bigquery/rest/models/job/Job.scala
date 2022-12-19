package com.permutive.google.bigquery.rest.models.job

import cats.data.NonEmptyList
import com.permutive.google.bigquery.rest.models.api.ErrorProtoApi
import com.permutive.google.bigquery.rest.models.api.job._
import com.permutive.google.bigquery.rest.models.job.JobState.Done
import com.permutive.google.bigquery.rest.models.job.NewTypes._

sealed trait Job {
  def id: JobId
  def state: JobState
}

object Job {

  private[rest] def fromResponse(response: QueryJobResponseApi): Job =
    from(response.jobReference.jobId, response.status)

  private[rest] def from(id: JobId, jobStatusApi: JobStatusApi): Job =
    jobStatusApi.state match {
      case Done => CompleteJob.from(id, jobStatusApi)
      case _    => IncompleteJob(id, jobStatusApi.state)
    }

}

sealed trait CompleteJob extends Job {
  final override val state = Done
}

object CompleteJob {

  // See documentation, errorResult is the true indication of failure
  // errors can be present in the list even if successful
  // https://cloud.google.com/bigquery/docs/reference/rest/v2/JobStatus
  private[rest] def from(id: JobId, jobStatusApi: JobStatusApi): CompleteJob =
    jobStatusApi.errorResult match {
      case None    => SuccessfulJob(id)
      case Some(e) => FailedJob.from(id, e, jobStatusApi.errors)
    }

}

final case class IncompleteJob(
  id: JobId,
  state: JobState,
) extends Job

final case class SuccessfulJob(
  id: JobId,
) extends CompleteJob

final case class FailedJob(
  id: JobId,
  jobError: JobError,
  jobErrors: NonEmptyList[JobError],
) extends CompleteJob

object FailedJob {

  private[rest] def from(id: JobId, e: ErrorProtoApi, es: Option[List[ErrorProtoApi]]): FailedJob =
    FailedJob(id, JobError.one(e), JobError.many(e, es))

}
