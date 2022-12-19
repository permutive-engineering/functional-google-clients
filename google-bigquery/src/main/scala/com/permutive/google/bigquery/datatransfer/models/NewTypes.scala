package com.permutive.google.bigquery.datatransfer.models

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

object NewTypes {

  // Documentation: https://cloud.google.com/appengine/docs/flexible/python/scheduling-jobs-with-cron-yaml
  @newtype case class Schedule(value: String)
  object Schedule {

    def daily(at: LocalTime): Schedule =
      Schedule(s"every day ${dailyTimeFormatter.format(at)}")

    private[this] val dailyTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    implicit val encoder: Encoder[Schedule] = deriving
    implicit val decoder: Decoder[Schedule] = deriving
  }

  @newtype case class DisplayName(value: String)
  object DisplayName {
    implicit val encoder: Encoder[DisplayName] = deriving
    implicit val decoder: Decoder[DisplayName] = deriving

    implicit val eq: Eq[DisplayName] = Eq.fromUniversalEquals
  }

  @newtype case class ConfigId(value: String)
  object ConfigId {
    implicit val encoder: Encoder[ConfigId] = deriving
    implicit val decoder: Decoder[ConfigId] = deriving
  }

}
