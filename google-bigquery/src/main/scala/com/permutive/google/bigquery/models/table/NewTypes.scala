package com.permutive.google.bigquery.models.table

import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

object NewTypes {

  @newtype case class TableId(value: String)
  object TableId {
    implicit val encoder: Encoder[TableId] = deriving
    implicit val decoder: Decoder[TableId] = deriving
  }

}
