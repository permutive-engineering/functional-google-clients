package com.permutive.google.bigquery.rest.models.api

import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models.table.NewTypes._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/tables#TableReference

private[rest] case class TableReferenceApi(
  projectId: BigQueryProjectName,
  datasetId: DatasetId,
  tableId: TableId,
)
private[rest] object TableReferenceApi {
  implicit val decoder: Decoder[TableReferenceApi]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[TableReferenceApi] = deriveEncoder
}
