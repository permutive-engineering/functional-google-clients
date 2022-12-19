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

package com.permutive.google.bigquery.rest.models

import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.Exceptions.BigQueryException
import com.permutive.google.bigquery.rest.models.job.FailedJob
import com.permutive.google.bigquery.rest.models.job.NewTypes.JobId
import com.permutive.google.bigquery.rest.models.job.results.QueryJobResults

import scala.concurrent.duration.FiniteDuration

object Exceptions {

  case class TimeoutException(
      id: JobId,
      pollDelay: FiniteDuration,
      pollCount: Int
  ) extends RuntimeException(
        s"Polling BigQuery job $id timed out after $pollCount polls with interval $pollDelay"
      )
      with BigQueryException

  case class MissingFieldsException(
      description: String,
      missingFields: NonEmptyList[String]
  ) extends RuntimeException(
        s"Failed to $description. Expected fields were missing: ${missingFields.toList.mkString(",")}"
      )
      with BigQueryException

  case class InvalidResultForSelectResultsException(
      typeReceived: String,
      jobId: JobId
  ) extends RuntimeException(
        s"Received invalid type ($typeReceived) when unrolling all select job results result for job `$jobId`"
      )
      with BigQueryException

  case class FailedJobException(job: FailedJob)
      extends RuntimeException(
        s"Failed job with id `${job.id}`. Received error ${job.jobError} and errors ${job.jobErrors.toList}"
      )
      with BigQueryException

  case class InvalidResultForDmlException(
      results: QueryJobResults,
      jobId: JobId
  ) extends RuntimeException(
        s"Expected DML results but retrieved different results. Received: $results"
      )
      with BigQueryException

}
