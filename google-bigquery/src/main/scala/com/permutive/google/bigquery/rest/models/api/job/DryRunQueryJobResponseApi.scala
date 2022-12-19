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
    statistics: DryRunQueryJobStatisticsApi
)

private[rest] object DryRunQueryJobResponseApi {
  implicit val decoder: Decoder[DryRunQueryJobResponseApi] = deriveDecoder
}
