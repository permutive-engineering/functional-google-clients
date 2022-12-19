package com.permutive.google.bigquery.rest.models.api.schema

import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models.table._
import com.permutive.google.bigquery.rest.models.api.TableReferenceApi
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/tables#Table

private[rest] case class CreateViewRequestApi(
  tableReference: TableReferenceApi,
  view: ViewApi,
)

private[rest] case class CreateViewResponseApi(
  tableReference: TableReferenceApi,
  schema: NonEmptyList[Field],
  location: Location,
  view: ViewApi,
  kind: String,
  etag: String,
  id: String,
  selfLink: String,
  creationTime: Long,
  lastModifiedTime: Long,
  `type`: TableType, // Should be View, just in case anyone wants to check
)

private[rest] case class ViewApi(
  query: Query,
  useLegacySql: Boolean,
)
private[rest] object ViewApi {
  implicit val decoder: Decoder[ViewApi]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[ViewApi] = deriveEncoder
}

private[rest] object CreateViewRequestApi {
  implicit val encoder: Encoder.AsObject[CreateViewRequestApi] = deriveEncoder
}

private[rest] object CreateViewResponseApi {
  implicit val decoder: Decoder[CreateViewResponseApi] = deriveDecoder
}
