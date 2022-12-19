package com.permutive.google.bigquery.datatransfer.models

import com.permutive.google.bigquery.datatransfer.models.NewTypes._
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models.WriteDisposition
import com.permutive.google.bigquery.models.table.Field
import com.permutive.google.bigquery.models.table.NewTypes._

case class ScheduleQueryRequest(
  displayName: DisplayName,
  query: Query,
  schedule: Schedule,
  destinationDataset: DatasetId,
  destinationTableName: TableId,
  writeDisposition: WriteDisposition,
  partitioningFieldName: Option[Field.Name],
)
