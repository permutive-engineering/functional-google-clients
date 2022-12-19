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
