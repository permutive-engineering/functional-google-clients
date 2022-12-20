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

package com.permutive.google.bigquery.rest.models.api.schema

import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.NewTypes.{BigQueryProjectName, DatasetId, Location}
import com.permutive.google.bigquery.models.schema.Access
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// https://cloud.google.com/bigquery/docs/reference/rest/v2/datasets#Dataset

private[rest] case class CreateDatasetRequestApi(
    datasetReference: DatasetReferenceApi,
    location: Location,
    access: Option[NonEmptyList[Access]],
    labels: Option[Map[String, String]]
)

private[rest] case class CreateDatasetResponseApi(
    kind: String,
    etag: String,
    id: String,
    selfLink: String,
    datasetReference: DatasetReferenceApi,
    location: Location,
    access: Option[NonEmptyList[Access]]
    // and more...
)

private[rest] object CreateDatasetResponseApi {
  implicit val decoder: Decoder[CreateDatasetResponseApi] = deriveDecoder
}

private[rest] case class DatasetReferenceApi(
    datasetId: DatasetId,
    projectId: BigQueryProjectName
)

private[rest] object DatasetReferenceApi {
  implicit val decoder: Decoder[DatasetReferenceApi] = deriveDecoder
  implicit val encoder: Encoder.AsObject[DatasetReferenceApi] = deriveEncoder
}

private[rest] object CreateDatasetRequestApi {
  implicit val encoder: Encoder.AsObject[CreateDatasetRequestApi] =
    deriveEncoder
}
