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
      case _ => IncompleteJob(id, jobStatusApi.state)
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
      case None => SuccessfulJob(id)
      case Some(e) => FailedJob.from(id, e, jobStatusApi.errors)
    }

}

final case class IncompleteJob(
    id: JobId,
    state: JobState
) extends Job

final case class SuccessfulJob(
    id: JobId
) extends CompleteJob

final case class FailedJob(
    id: JobId,
    jobError: JobError,
    jobErrors: NonEmptyList[JobError]
) extends CompleteJob

object FailedJob {

  private[rest] def from(
      id: JobId,
      e: ErrorProtoApi,
      es: Option[List[ErrorProtoApi]]
  ): FailedJob =
    FailedJob(id, JobError.one(e), JobError.many(e, es))

}
