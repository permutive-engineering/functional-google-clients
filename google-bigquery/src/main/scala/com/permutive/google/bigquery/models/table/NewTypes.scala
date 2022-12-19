package com.permutive.google.bigquery.models.table

import io.circe.{Decoder, Encoder}

object NewTypes {

  case class TableId(value: String) extends AnyVal
  object TableId {
    implicit val encoder: Encoder[TableId] =
      Encoder.encodeString.contramap(_.value)
    implicit val decoder: Decoder[TableId] =
      Decoder.decodeString.map(TableId(_))
  }

}
