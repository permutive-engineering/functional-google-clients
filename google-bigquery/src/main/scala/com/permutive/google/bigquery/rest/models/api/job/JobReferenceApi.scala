package com.permutive.google.bigquery.rest.models.api.job

import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.rest.models.job.NewTypes._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/JobReference

// In order to specify the location we must give some form of job ID
// https://cloud.google.com/bigquery/docs/reference/rest/v2/jobs
private[rest] case class JobReferenceApi(
  jobId: JobId,
  location: Option[Location],
  projectId: BigQueryProjectName,
)

private[rest] object JobReferenceApi {
  implicit val decoder: Decoder[JobReferenceApi]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[JobReferenceApi] = deriveEncoder
}
