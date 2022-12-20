/*
 * Copyright 2022 Permutive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    projectId: BigQueryProjectName
)

private[rest] object JobReferenceApi {
  implicit val decoder: Decoder[JobReferenceApi] = deriveDecoder
  implicit val encoder: Encoder.AsObject[JobReferenceApi] = deriveEncoder
}
