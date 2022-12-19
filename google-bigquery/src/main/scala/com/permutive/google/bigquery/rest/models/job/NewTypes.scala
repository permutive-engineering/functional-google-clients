package com.permutive.google.bigquery.rest.models.job

import io.circe.{Decoder, Encoder}

object NewTypes {

  case class JobId(value: String) extends AnyVal
  object JobId {
    implicit val decoder: Decoder[JobId] = Decoder.decodeString.map(JobId(_))
    implicit val encoder: Encoder[JobId] =
      Encoder.encodeString.contramap(_.value)
  }

}
