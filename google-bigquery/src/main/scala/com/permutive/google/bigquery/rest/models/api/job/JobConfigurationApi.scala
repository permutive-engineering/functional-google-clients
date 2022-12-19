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

import enumeratum.EnumEntry.Lowercase
import enumeratum.{Circe, Enum, EnumEntry}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe._

import scala.collection.immutable

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/JobConfiguration

sealed private[rest] trait JobConfigurationApi

object JobConfigurationApi {

  final private[rest] case class Query(
      query: JobConfigurationQueryApi,
      dryRun: Option[Boolean] = None
  ) extends JobConfigurationApi
  object Query {
    implicit val decoder: Decoder[Query] = deriveDecoder
    implicit val encoder: Encoder.AsObject[Query] = deriveEncoder
  }

  private[rest] case object Copy extends JobConfigurationApi
  private[rest] case object Extract extends JobConfigurationApi
  private[rest] case object Load extends JobConfigurationApi
  private[rest] case object Unknown extends JobConfigurationApi

  // Cannot use circe.generic.extras as it is case _sensitive_ in the indicator
  implicit val decoder: Decoder[JobConfigurationApi] =
    new Decoder[JobConfigurationApi] {
      override def apply(
          c: HCursor
      ): Either[DecodingFailure, JobConfigurationApi] =
        // Decode the indicator and if that is OK decode the body
        JobTypeIndicatorApi.decoder(c).flatMap {
          case JobTypeIndicatorApi(JobTypeIndicator.Copy) =>
            Right(JobConfigurationApi.Copy)
          case JobTypeIndicatorApi(JobTypeIndicator.Extract) =>
            Right(JobConfigurationApi.Extract)
          case JobTypeIndicatorApi(JobTypeIndicator.Load) =>
            Right(JobConfigurationApi.Load)
          case JobTypeIndicatorApi(JobTypeIndicator.Query) => Query.decoder(c)
          case JobTypeIndicatorApi(JobTypeIndicator.Unknown) =>
            Right(JobConfigurationApi.Unknown)
        }
    }

  implicit val encoder: Encoder.AsObject[JobConfigurationApi] =
    new Encoder.AsObject[JobConfigurationApi] {
      override def encodeObject(jca: JobConfigurationApi): JsonObject =
        jca match {
          case q: Query =>
            indicatorJson(JobTypeIndicator.Query).deepMerge(q.asJsonObject)
          case Copy    => indicatorJson(JobTypeIndicator.Copy)
          case Extract => indicatorJson(JobTypeIndicator.Extract)
          case Load    => indicatorJson(JobTypeIndicator.Load)
          case Unknown => indicatorJson(JobTypeIndicator.Unknown)
        }

      private def indicatorJson(indicator: JobTypeIndicator): JsonObject =
        JobTypeIndicatorApi(indicator).asJsonObject
    }

  final private[this] case class JobTypeIndicatorApi(
      jobType: JobTypeIndicator
  )
  private[this] object JobTypeIndicatorApi {
    implicit val decoder: Decoder[JobTypeIndicatorApi] = deriveDecoder
    implicit val encoder: Encoder.AsObject[JobTypeIndicatorApi] = deriveEncoder
  }

  sealed private[this] trait JobTypeIndicator extends EnumEntry with Lowercase
  private[this] object JobTypeIndicator extends Enum[JobTypeIndicator] {
    override val values: immutable.IndexedSeq[JobTypeIndicator] = findValues

    case object Copy extends JobTypeIndicator
    case object Extract extends JobTypeIndicator
    case object Load extends JobTypeIndicator
    case object Query extends JobTypeIndicator
    case object Unknown extends JobTypeIndicator

    // Not mixing in CirceEnum so that we can enforce case-insensitivity when decoding
    implicit val decoder: Decoder[JobTypeIndicator] =
      Circe.decodeCaseInsensitive(this)
    implicit val encoder: Encoder[JobTypeIndicator] = Circe.encoder(this)
  }

}
