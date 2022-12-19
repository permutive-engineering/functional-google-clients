package com.permutive.google.bigquery.rest.models.api.job

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/Job

private[rest] case class QueryJobResponseApi(
  status: JobStatusApi,
  jobReference: JobReferenceApi,
  configuration: JobConfigurationApi,
  kind: String,
  etag: String,
  selfLink: Option[String],
)

private[rest] object QueryJobResponseApi {
  implicit val decoder: Decoder[QueryJobResponseApi] = deriveDecoder
}
