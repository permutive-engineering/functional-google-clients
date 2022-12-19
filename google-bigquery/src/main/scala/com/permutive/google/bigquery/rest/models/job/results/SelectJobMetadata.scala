package com.permutive.google.bigquery.rest.models.job.results

import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.NewTypes.Location
import com.permutive.google.bigquery.models.table.Field
import com.permutive.google.bigquery.rest.models.Cost
import com.permutive.google.bigquery.rest.models.job.JobError
import io.scalaland.chimney.dsl._

case class SelectJobMetadata(
  schema: NonEmptyList[Field],
  totalRows: Long,
  location: Option[Location],
  totalBytesProcessed: Long,
  cost: Cost,
  cacheHit: Boolean,
  errors: Option[NonEmptyList[JobError]],
)

object SelectJobMetadata {

  def fromCompleteSelectJob(completeSelectJob: CompleteSelectJob): SelectJobMetadata =
    completeSelectJob.transformInto[SelectJobMetadata]

}
