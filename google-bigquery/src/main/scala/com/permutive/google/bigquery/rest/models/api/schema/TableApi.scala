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

import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models.table._
import com.permutive.google.bigquery.rest.models.api.{SchemaApi, TableReferenceApi}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/tables#Table
// the `Table` type is used when creating and updating tables

private[rest] case class TableRequestApi(
    tableReference: TableReferenceApi,
    schema: SchemaApi,
    timePartitioning: Option[Partitioning]
)

private[rest] case class TableResponseApi(
    tableReference: TableReferenceApi,
    schema: SchemaApi,
    location: Location,
    timePartitioning: Option[Partitioning],
    kind: String,
    etag: String,
    id: String,
    selfLink: String,
    numBytes: Long,
    numLongTermBytes: Long,
    numRows: Long,
    creationTime: Long,
    lastModifiedTime: Long,
    `type`: TableType // Should be Table, just in case anyone wants to check
)

private[rest] object TableRequestApi {
  implicit val encoder: Encoder.AsObject[TableRequestApi] = deriveEncoder
}

private[rest] object TableResponseApi {
  implicit val decoder: Decoder[TableResponseApi] = deriveDecoder
}
