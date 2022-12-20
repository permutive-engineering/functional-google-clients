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

package com.permutive.google.bigquery.rest.models.api

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/ErrorProto

private[rest] case class ErrorProtoApi(
    reason: String,
    location: Option[String],
//  debugInfo: String,  // Specified as internal so should not be used
    message: String
)

private[rest] object ErrorProtoApi {
  implicit val decoder: Decoder[ErrorProtoApi] = deriveDecoder
  implicit val encoder: Encoder.AsObject[ErrorProtoApi] = deriveEncoder
}
