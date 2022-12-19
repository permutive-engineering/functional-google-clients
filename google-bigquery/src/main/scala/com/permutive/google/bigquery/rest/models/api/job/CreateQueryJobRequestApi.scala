package com.permutive.google.bigquery.rest.models.api.job

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/Job

private[rest] case class CreateQueryJobRequestApi(
  configuration: JobConfigurationApi,
  jobReference: JobReferenceApi,
)

private[rest] object CreateQueryJobRequestApi {
  implicit val decoder: Decoder[CreateQueryJobRequestApi]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[CreateQueryJobRequestApi] = deriveEncoder
}
