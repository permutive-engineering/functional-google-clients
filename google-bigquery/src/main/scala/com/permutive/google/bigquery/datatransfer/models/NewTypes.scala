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

package com.permutive.google.bigquery.datatransfer.models

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import cats.Eq
import io.circe.{Decoder, Encoder}

object NewTypes {

  // Documentation: https://cloud.google.com/appengine/docs/flexible/python/scheduling-jobs-with-cron-yaml
  case class Schedule(value: String) extends AnyVal
  object Schedule {

    def daily(at: LocalTime): Schedule =
      Schedule(s"every day ${dailyTimeFormatter.format(at)}")

    private[this] val dailyTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    implicit val encoder: Encoder[Schedule] =
      Encoder.encodeString.contramap(_.value)
    implicit val decoder: Decoder[Schedule] =
      Decoder.decodeString.map(Schedule(_))
  }

  case class DisplayName(value: String) extends AnyVal
  object DisplayName {
    implicit val encoder: Encoder[DisplayName] =
      Encoder.encodeString.contramap(_.value)
    implicit val decoder: Decoder[DisplayName] =
      Decoder.decodeString.map(DisplayName(_))

    implicit val eq: Eq[DisplayName] = Eq.fromUniversalEquals
  }

  case class ConfigId(value: String) extends AnyVal
  object ConfigId {
    implicit val encoder: Encoder[ConfigId] =
      Encoder.encodeString.contramap(_.value)
    implicit val decoder: Decoder[ConfigId] =
      Decoder.decodeString.map(ConfigId(_))
  }

}
