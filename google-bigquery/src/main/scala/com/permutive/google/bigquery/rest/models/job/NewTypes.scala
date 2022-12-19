package com.permutive.google.bigquery.rest.models.job

import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

object NewTypes {

  @newtype case class JobId(value: String)
  object JobId {
    implicit val decoder: Decoder[JobId] = deriving
    implicit val encoder: Encoder[JobId] = deriving
  }

}
