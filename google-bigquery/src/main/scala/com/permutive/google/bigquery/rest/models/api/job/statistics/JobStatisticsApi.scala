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

import com.permutive.google.bigquery.rest.models.api.TypeFormat.Int64Value
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/Job#JobStatistics

// May be worth not trying to combine these types at all.
// Only use case at the moment is dry running queries, have not yet needed to report on completed queries.

sealed private[rest] trait JobStatisticsApi {
  def creationTime: Int64Value
  def totalBytesProcessed: Int64Value
  def startTime: Option[Int64Value]
  def endTime: Option[Int64Value]
}

private[rest] object JobStatisticsApi {
  implicit val decoder: Decoder[JobStatisticsApi] =
    QueryJobStatisticApi.decoder.map(identity)

  implicit val encoder: Encoder[JobStatisticsApi] = Encoder.instance { case qj: QueryJobStatisticApi =>
    QueryJobStatisticApi.encoder(qj)
  }
}

sealed private[rest] trait QueryJobStatisticApi extends JobStatisticsApi {
  def query: QueryStatisticsApi
}

private[rest] object QueryJobStatisticApi {
  implicit val decoder: Decoder[QueryJobStatisticApi] =
    DryRunQueryJobStatisticsApi.decoder.map(identity)

  implicit val encoder: Encoder[QueryJobStatisticApi] = Encoder.instance { case d: DryRunQueryJobStatisticsApi =>
    DryRunQueryJobStatisticsApi.encoder(d)
  }
}

final private[rest] case class DryRunQueryJobStatisticsApi(
    totalBytesProcessed: Int64Value,
    query: DryQueryStatisticsApi,
    creationTime: Int64Value,
    startTime: Option[Int64Value],
    endTime: Option[Int64Value]
) extends QueryJobStatisticApi

private[rest] object DryRunQueryJobStatisticsApi {
  implicit val decoder: Decoder[DryRunQueryJobStatisticsApi] = deriveDecoder
  implicit val encoder: Encoder.AsObject[DryRunQueryJobStatisticsApi] =
    deriveEncoder
}
