package com.permutive.google.bigquery.models.table

import cats.Eq
import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.SQLType
import enumeratum.EnumEntry.Uppercase
import enumeratum.{CirceEnum, Enum, EnumEntry}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

// Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/tables#resource

case class Field(
  name: Field.Name,
  `type`: SQLType,
  mode: Option[Field.Mode],
  description: Option[String],
  fields: Option[NonEmptyList[Field]],
)

object Field {

  @newtype case class Name(value: String)

  object Name {
    implicit val decoder: Decoder[Name] = deriving
    implicit val encoder: Encoder[Name] = deriving

    implicit val eq: Eq[Name] = Eq.fromUniversalEquals
  }

  sealed trait Mode extends EnumEntry with Uppercase

  object Mode extends Enum[Mode] with CirceEnum[Mode] {
    override val values = findValues

    case object Nullable extends Mode

    case object Repeated extends Mode

    case object Required extends Mode

  }

  implicit val decoder: Decoder[Field]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[Field] = deriveEncoder

}
