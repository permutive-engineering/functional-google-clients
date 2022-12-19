package com.permutive.google.bigquery.rest.models.api

import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.table.Field
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/tables#TableSchema

private[rest] case class SchemaApi(
  fields: NonEmptyList[Field],
)
private[rest] object SchemaApi {
  implicit val decoder: Decoder[SchemaApi]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[SchemaApi] = deriveEncoder
}
