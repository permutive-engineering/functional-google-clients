package com.permutive.google.bigquery.datatransfer.models

import com.permutive.google.bigquery.datatransfer.models.NewTypes.DisplayName
import com.permutive.google.bigquery.models.NewTypes.DatasetId
import io.scalaland.chimney.dsl._

case class ScheduledQueryIdentity(
  displayName: DisplayName,
  destinationDataset: DatasetId,
)
object ScheduledQueryIdentity {

  def fromScheduleQueryRequest(sqr: ScheduleQueryRequest): ScheduledQueryIdentity =
    sqr.into[ScheduledQueryIdentity].transform

  def fromScheduledQuery(sq: ScheduledQuery): ScheduledQueryIdentity =
    sq.into[ScheduledQueryIdentity].transform

}
