package com.permutive.google.bigquery.rest.models.job.results

import io.circe.{Decoder, Encoder, Json}
import io.estatico.newtype.macros.newtype

object NewTypes {

  @newtype case class PageToken(value: String)
  object PageToken {
    implicit val decoder: Decoder[PageToken] = deriving
    implicit val encoder: Encoder[PageToken] = deriving
  }

  @newtype case class JobResultRow(value: Json)

}
