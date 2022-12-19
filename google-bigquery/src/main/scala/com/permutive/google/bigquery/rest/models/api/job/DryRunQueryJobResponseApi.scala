package com.permutive.google.bigquery.rest.models.api.job

import com.permutive.google.bigquery.rest.models.api.job.statistics.DryRunQueryJobStatisticsApi
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/Job

// Separated types as I see little value in working to combine them
private[rest] case class DryRunQueryJobResponseApi(
  status: JobStatusApi,
  jobReference: DryRunJobReferenceApi,
  configuration: JobConfigurationApi,
  kind: String,
  etag: String,
  selfLink: Option[String],
  statistics: DryRunQueryJobStatisticsApi,
)

private[rest] object DryRunQueryJobResponseApi {
  implicit val decoder: Decoder[DryRunQueryJobResponseApi] = deriveDecoder
}
