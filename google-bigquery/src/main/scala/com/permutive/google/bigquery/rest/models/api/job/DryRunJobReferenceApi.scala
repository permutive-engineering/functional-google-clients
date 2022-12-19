package com.permutive.google.bigquery.rest.models.api.job

import com.permutive.google.bigquery.models.NewTypes.{BigQueryProjectName, Location}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/JobReference

// Manually determined to not have a JobId (through testing this library and in the API documentation "Try this API")
private[rest] case class DryRunJobReferenceApi(
  location: Option[Location],
  projectId: BigQueryProjectName,
)

private[rest] object DryRunJobReferenceApi {
  implicit val decoder: Decoder[DryRunJobReferenceApi]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[DryRunJobReferenceApi] = deriveEncoder
}
