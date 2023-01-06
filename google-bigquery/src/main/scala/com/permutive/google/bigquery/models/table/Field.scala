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
import cats.syntax.functor._
import com.permutive.google.bigquery.models.SQLType
import enumeratum.EnumEntry.Uppercase
import enumeratum.{CirceEnum, Enum, EnumEntry}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/tables#resource

sealed abstract class Field private (
    val name: Field.Name,
    val `type`: SQLType,
    val mode: Option[Field.Mode],
    val description: Option[String],
    val fields: Option[NonEmptyList[Field]]
) {
  override def equals(obj: Any): Boolean = obj match {
    case f: Field => Eq[Field].eqv(this, f)
    case _ => false
  }
}

object Field {

  def apply(
      name: Field.Name,
      `type`: SQLType,
      mode: Option[Field.Mode],
      description: Option[String],
      fields: Option[NonEmptyList[Field]]
  ): Field = new Field(name, `type`, mode, description, fields) {}

  // Exists so that Circe instances can be derived for Scala 3
  sealed private case class FieldCC private (
      override val name: Field.Name,
      override val `type`: SQLType,
      override val mode: Option[Field.Mode],
      override val description: Option[String],
      override val fields: Option[NonEmptyList[Field]]
  ) extends Field(name, `type`, mode, description, fields)

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

  // Eq instance that works for both the abstract class and case class
  implicit val eq: Eq[Field] = Eq.instance { (x, y) =>
    x.name == y.name && x.`type` == y.`type` && x.mode == y.mode && x.description == y.description && x.fields == y.fields
  }

  implicit val decoder: Decoder[Field] = deriveDecoder[FieldCC].widen
  implicit val encoder: Encoder[Field] =
    deriveEncoder[FieldCC].contramap[Field](f => FieldCC(f.name, f.`type`, f.mode, f.description, f.fields))

}
