package com.permutive.google.bigquery.datatransfer

import com.permutive.google.bigquery.datatransfer.models.NewTypes._
import com.permutive.google.bigquery.datatransfer.models._
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models.WriteDisposition
import com.permutive.google.bigquery.models.table.Field
import com.permutive.google.bigquery.models.table.NewTypes._

trait BigQueryDataTransfer[F[_]] {

  def scheduleQuery(
      displayName: DisplayName,
      query: Query,
      schedule: Schedule,
      destinationDataset: DatasetId,
      destinationTableName: TableId,
      writeDisposition: WriteDisposition,
      partitioningFieldName: Option[Field.Name]
  ): F[Unit]

  def scheduleQuery(scheduleQueryRequest: ScheduleQueryRequest): F[Unit]

  def scheduleQueries(requests: List[ScheduleQueryRequest]): F[Unit]

  def getScheduledQuery(configId: ConfigId): F[Option[ScheduledQuery]]

  def getScheduledQueries: F[List[ScheduledQuery]]

  def findScheduledQueries(
      displayName: DisplayName,
      destinationDataset: DatasetId
  ): F[List[ScheduledQuery]]

  def updateScheduledQuery(
      configId: ConfigId,
      query: Query,
      destinationTableName: Option[TableId],
      writeDisposition: Option[WriteDisposition],
      partitioningFieldName: Option[Field.Name]
  ): F[Unit]

}

object BigQueryDataTransfer {
  def apply[F[_]: BigQueryDataTransfer]: BigQueryDataTransfer[F] = implicitly
}
