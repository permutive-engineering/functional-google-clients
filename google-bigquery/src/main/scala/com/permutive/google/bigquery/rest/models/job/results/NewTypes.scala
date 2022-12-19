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
