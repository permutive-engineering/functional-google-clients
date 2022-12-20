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

package com.permutive.google.bigquery.rest.models.api.job.statistics

import cats.data.NonEmptyList
import com.permutive.google.bigquery.rest.models.api.TypeFormat.Int64Value
import com.permutive.google.bigquery.rest.models.api.{SchemaApi, TableReferenceApi}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/Job#JobStatistics2

sealed private[rest] trait QueryStatisticsApi {
  def cacheHit: Boolean
  def statementType: String
  def totalBytesProcessed: Int64Value
  def totalBytesBilled: Int64Value
  def referencedTables: Option[NonEmptyList[TableReferenceApi]]
}

private[rest] object QueryStatisticsApi {
  implicit val decoder: Decoder[QueryStatisticsApi] =
    DryQueryStatisticsApi.decoder.map(identity)

  implicit val encoder: Encoder.AsObject[QueryStatisticsApi] = { case d: DryQueryStatisticsApi =>
    DryQueryStatisticsApi.encoder.encodeObject(d)
  }
}

final private[rest] case class DryQueryStatisticsApi(
    totalBytesBilled: Int64Value,
    totalBytesProcessed: Int64Value,
    totalBytesProcessedAccuracy: BytesProcessedAccuracy,
    schema: Option[SchemaApi],
    cacheHit: Boolean,
    statementType: String,
    referencedTables: Option[NonEmptyList[TableReferenceApi]]
) extends QueryStatisticsApi

private[rest] object DryQueryStatisticsApi {
  implicit val decoder: Decoder[DryQueryStatisticsApi] = deriveDecoder
  implicit val encoder: Encoder.AsObject[DryQueryStatisticsApi] = deriveEncoder
}
