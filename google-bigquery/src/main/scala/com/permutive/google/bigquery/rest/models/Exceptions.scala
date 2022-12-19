package com.permutive.google.bigquery.rest.models

import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.Exceptions.BigQueryException
import com.permutive.google.bigquery.rest.models.job.FailedJob
import com.permutive.google.bigquery.rest.models.job.NewTypes.JobId
import com.permutive.google.bigquery.rest.models.job.results.QueryJobResults

import scala.concurrent.duration.FiniteDuration

object Exceptions {

  case class TimeoutException(id: JobId, pollDelay: FiniteDuration, pollCount: Int)
      extends RuntimeException(s"Polling BigQuery job $id timed out after $pollCount polls with interval $pollDelay")
      with BigQueryException

  case class MissingFieldsException(description: String, missingFields: NonEmptyList[String])
      extends RuntimeException(
        s"Failed to $description. Expected fields were missing: ${missingFields.toList.mkString(",")}",
      )
      with BigQueryException

  case class InvalidResultForSelectResultsException(typeReceived: String, jobId: JobId)
      extends RuntimeException(
        s"Received invalid type ($typeReceived) when unrolling all select job results result for job `$jobId`",
      )
      with BigQueryException

  case class FailedJobException(job: FailedJob)
      extends RuntimeException(
        s"Failed job with id `${job.id}`. Received error ${job.jobError} and errors ${job.jobErrors.toList}",
      )
      with BigQueryException

  case class InvalidResultForDmlException(results: QueryJobResults, jobId: JobId)
      extends RuntimeException(s"Expected DML results but retrieved different results. Received: $results")
      with BigQueryException

}
