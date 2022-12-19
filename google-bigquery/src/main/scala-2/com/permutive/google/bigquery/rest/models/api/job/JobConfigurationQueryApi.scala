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

import cats.data.NonEmptyList
import cats.syntax.all._
import com.permutive.google.bigquery.models.NewTypes.Query
import com.permutive.google.bigquery.models.WriteDisposition
import com.permutive.google.bigquery.rest.models.api.TableReferenceApi
import com.permutive.google.bigquery.rest.models.job.queryparameters.QueryParameter
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/JobConfiguration#JobConfigurationQuery

sealed private[rest] trait JobConfigurationQueryApi {
  def query: Query
  def useLegacySql: Boolean
}

object JobConfigurationQueryApi {

  implicit val encoder: Encoder.AsObject[JobConfigurationQueryApi] = {
    case writeTable: JobConfigurationQueryWriteTableApi =>
      writeTable.asJsonObject
    case basic: JobConfigurationQueryBasicApi => basic.asJsonObject
  }

  // Want to decode the most specific that we can first
  implicit val decoder: Decoder[JobConfigurationQueryApi] =
    List[Decoder[JobConfigurationQueryApi]](
      JobConfigurationQueryWriteTableApi.decoder.widen,
      JobConfigurationQueryBasicApi.decoder.widen
    ).reduceLeft(_.or(_))
}

final private[rest] case class JobConfigurationQueryBasicApi(
    query: Query,
    useLegacySql: Boolean,
    queryParameters: Option[NonEmptyList[QueryParameter]]
) extends JobConfigurationQueryApi

private[rest] object JobConfigurationQueryBasicApi {
  implicit val encoder: Encoder.AsObject[JobConfigurationQueryBasicApi] =
    deriveEncoder
  implicit val decoder: Decoder[JobConfigurationQueryBasicApi] = deriveDecoder
}

final private[rest] case class JobConfigurationQueryWriteTableApi(
    query: Query,
    writeDisposition: WriteDisposition,
    destinationTable: TableReferenceApi,
    useLegacySql: Boolean
) extends JobConfigurationQueryApi

private[rest] object JobConfigurationQueryWriteTableApi {
  implicit val encoder: Encoder.AsObject[JobConfigurationQueryWriteTableApi] =
    deriveEncoder
  implicit val decoder: Decoder[JobConfigurationQueryWriteTableApi] =
    deriveDecoder
}
