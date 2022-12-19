package com.permutive.google.bigquery.rest.models.api

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/ErrorProto

private[rest] case class ErrorProtoApi(
  reason: String,
  location: Option[String],
//  debugInfo: String,  // Specified as internal so should not be used
  message: String,
)

private[rest] object ErrorProtoApi {
  implicit val decoder: Decoder[ErrorProtoApi]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[ErrorProtoApi] = deriveEncoder
}
