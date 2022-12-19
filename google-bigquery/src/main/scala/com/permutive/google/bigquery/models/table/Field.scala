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

package com.permutive.google.bigquery.models.table

import cats.Eq
import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.SQLType
import enumeratum.EnumEntry.Uppercase
import enumeratum.{CirceEnum, Enum, EnumEntry}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/tables#resource

case class Field(
    name: Field.Name,
    `type`: SQLType,
    mode: Option[Field.Mode],
    description: Option[String],
    fields: Option[NonEmptyList[Field]]
)

object Field {

  case class Name(value: String) extends AnyVal

  object Name {
    implicit val decoder: Decoder[Name] = Decoder.decodeString.map(Name(_))
    implicit val encoder: Encoder[Name] =
      Encoder.encodeString.contramap(_.value)

    implicit val eq: Eq[Name] = Eq.fromUniversalEquals
  }

  sealed trait Mode extends EnumEntry with Uppercase

  object Mode extends Enum[Mode] with CirceEnum[Mode] {
    override val values = findValues

    case object Nullable extends Mode

    case object Repeated extends Mode

    case object Required extends Mode

  }

  implicit val decoder: Decoder[Field] = deriveDecoder
  implicit val encoder: Encoder.AsObject[Field] = deriveEncoder

}
