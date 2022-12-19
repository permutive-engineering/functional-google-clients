package com.permutive.google.bigquery.rest.models.api.job

import com.permutive.google.bigquery.rest.models.api.TypeFormat._
import com.permutive.google.bigquery.rest.models.api.{ErrorProtoApi, SchemaApi}
import com.permutive.google.bigquery.rest.models.job.results.NewTypes._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/jobs/getQueryResults
//
// Cases of fields being present done by poking the API documentation
// No comment means present under all circumstances

final private[rest] case class JobQueryResultApi(
  kind: String,
  etag: String,
  schema: Option[SchemaApi], // Present in success cases
  jobReference: JobReferenceApi,
  totalRows: Option[UInt64Value],          // Present in success cases
  pageToken: Option[PageToken],            // Present in success cases when there are additional results
  rows: Option[List[Json]],                // Present in success cases when rows are available, not present if 0 rows returned
  totalBytesProcessed: Option[Int64Value], // Present in a complete job
  jobComplete: Boolean,
  errors: Option[List[ErrorProtoApi]],    // Presence does not necessarily mean job has failed
  cacheHit: Option[Boolean],              // Present in a complete job
  numDmlAffectedRows: Option[Int64Value], // Present only for successful INSERT, UPDATE and DELETE jobs
)

private[rest] object JobQueryResultApi {
  implicit val decoder: Decoder[JobQueryResultApi]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[JobQueryResultApi] = deriveEncoder
}
