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

package com.permutive.google.bigquery.rest.models.job.results

import io.circe.{Decoder, Encoder, Json}

object NewTypes {

  case class PageToken(value: String) extends AnyVal
  object PageToken {
    implicit val decoder: Decoder[PageToken] =
      Decoder.decodeString.map(PageToken(_))
    implicit val encoder: Encoder[PageToken] =
      Encoder.encodeString.contramap(_.value)
  }

  case class JobResultRow(value: Json) extends AnyVal

}
