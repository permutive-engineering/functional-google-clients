package com.permutive.google.bigquery.rest.models.api.schema

import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models.table._
import com.permutive.google.bigquery.rest.models.api.{SchemaApi, TableReferenceApi}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/tables#Table
// the `Table` type is used when creating and updating tables

private[rest] case class TableRequestApi(
  tableReference: TableReferenceApi,
  schema: SchemaApi,
  timePartitioning: Option[Partitioning],
)

private[rest] case class TableResponseApi(
  tableReference: TableReferenceApi,
  schema: SchemaApi,
  location: Location,
  timePartitioning: Option[Partitioning],
  kind: String,
  etag: String,
  id: String,
  selfLink: String,
  numBytes: Long,
  numLongTermBytes: Long,
  numRows: Long,
  creationTime: Long,
  lastModifiedTime: Long,
  `type`: TableType, // Should be Table, just in case anyone wants to check
)

private[rest] object TableRequestApi {
  implicit val encoder: Encoder.AsObject[TableRequestApi] = deriveEncoder
}

private[rest] object TableResponseApi {
  implicit val decoder: Decoder[TableResponseApi] = deriveDecoder
}
