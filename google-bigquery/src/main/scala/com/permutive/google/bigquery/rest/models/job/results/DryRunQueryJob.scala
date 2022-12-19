package com.permutive.google.bigquery.rest.models.job.results

import java.time.Instant

import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.NewTypes.Location
import com.permutive.google.bigquery.models.table.Field
import com.permutive.google.bigquery.rest.models.{Cost, TableReference}
import com.permutive.google.bigquery.rest.models.api.TypeFormat
import com.permutive.google.bigquery.rest.models.api.job.DryRunQueryJobResponseApi
import com.permutive.google.bigquery.rest.models.api.job.statistics.BytesProcessedAccuracy
import com.permutive.google.bigquery.rest.models.job.{JobError, JobState}

/**
  * Represents the results of dry-running a query.
  *
  * @param totalBytes Total number of bytes the job would run if executed.
  * @param queryBytes Total number of bytes the query would run if executed.
  * @param queryBytesBilled Total number of bytes billed for this dry-run (should be 0)
  * @param queryBytesAccuracy Accuracy of the estimate of query bytes provided by BigQuery.
  * @param location Location that the query will run in (billing).
  * @param referencedTables Any tables the query references.
  * @param schema The return schema of the query.
  * @param cacheHit If the job would hit a cache when running.
  * @param state State of the dry-run job (should be completed).
  * @param errors Any errors that resulted from dry-running the query.
  */
case class DryRunQueryJob(
  totalBytes: Long,
  queryBytes: Long, // Seems to be the same as queryBytes
  queryBytesBilled: Long,
  queryBytesAccuracy: BytesProcessedAccuracy,
  location: Option[Location],
  referencedTables: Option[NonEmptyList[TableReference]],
  schema: Option[NonEmptyList[Field]],
  creationTime: Instant,
  cacheHit: Boolean,
  state: JobState,
  errors: Option[NonEmptyList[JobError]],
) {

  val totalCost: Cost  = Cost(totalBytes, location)
  val queryCost: Cost  = Cost(queryBytes, location)
  val billedCost: Cost = Cost(queryBytesBilled, location)

}

object DryRunQueryJob {

  private[rest] def fromResponse(response: DryRunQueryJobResponseApi): DryRunQueryJob =
    DryRunQueryJob(
      response.statistics.totalBytesProcessed.value,
      response.statistics.query.totalBytesProcessed.value,
      response.statistics.query.totalBytesBilled.value,
      response.statistics.query.totalBytesProcessedAccuracy,
      response.jobReference.location,
      response.statistics.query.referencedTables.map(_.map(TableReference.fromApi)),
      response.statistics.query.schema.map(_.fields),
      TypeFormat.Converters.instantFromTime(response.statistics.creationTime),
      response.statistics.query.cacheHit,
      response.status.state,
      JobError.many(response.status.errorResult, response.status.errors),
    )

}
