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
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models.table._
import com.permutive.google.bigquery.rest.models.api.TableReferenceApi
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/tables#Table

private[rest] case class CreateViewRequestApi(
    tableReference: TableReferenceApi,
    view: ViewApi
)

private[rest] case class CreateViewResponseApi(
    tableReference: TableReferenceApi,
    schema: NonEmptyList[Field],
    location: Location,
    view: ViewApi,
    kind: String,
    etag: String,
    id: String,
    selfLink: String,
    creationTime: Long,
    lastModifiedTime: Long,
    `type`: TableType // Should be View, just in case anyone wants to check
)

private[rest] case class ViewApi(
    query: Query,
    useLegacySql: Boolean
)
private[rest] object ViewApi {
  implicit val decoder: Decoder[ViewApi] = deriveDecoder
  implicit val encoder: Encoder.AsObject[ViewApi] = deriveEncoder
}

private[rest] object CreateViewRequestApi {
  implicit val encoder: Encoder.AsObject[CreateViewRequestApi] = deriveEncoder
}

private[rest] object CreateViewResponseApi {
  implicit val decoder: Decoder[CreateViewResponseApi] = deriveDecoder
}
