package com.permutive.google.bigquery.rest.models.api.job

import com.permutive.google.bigquery.rest.models.api.ErrorProtoApi
import com.permutive.google.bigquery.rest.models.job.JobState
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/JobStatus

private[rest] case class JobStatusApi(
  state: JobState,
  errorResult: Option[ErrorProtoApi],
  errors: Option[List[ErrorProtoApi]],
)

private[rest] object JobStatusApi {
  implicit val encoder: Encoder.AsObject[JobStatusApi] = deriveEncoder
  implicit val decoder: Decoder[JobStatusApi]          = deriveDecoder
}
