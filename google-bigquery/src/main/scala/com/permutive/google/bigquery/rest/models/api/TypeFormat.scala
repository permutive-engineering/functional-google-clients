package com.permutive.google.bigquery.rest.models.api

import java.time.Instant

import io.circe.{Decoder, Encoder}

import scala.util.Try

// API Documentation: https://developers.google.com/discovery/v1/type-format

private[rest] object TypeFormat {

  case class Int64Value(value: Long) extends AnyVal
  object Int64Value {
    implicit val decoder: Decoder[Int64Value] =
      deriveDecoder[String, Int64Value](s => Try(Int64Value(s.toLong)))
    implicit val encoder: Encoder[Int64Value] =
      deriveEncoder[String, Int64Value](_.value.toString)
  }

  // This is unsigned, can not (easily) do that though, the Java library also uses built in signed long
  // e.g. https://github.com/googleapis/google-cloud-java/blob/675ace7601e2c09474ce986d5d2223af5cb15f28/google-cloud-clients/google-cloud-bigquery/src/main/java/com/google/cloud/bigquery/TableResult.java
  case class UInt64Value(value: Long) extends AnyVal
  object UInt64Value {
    implicit val decoder: Decoder[UInt64Value] =
      deriveDecoder[String, UInt64Value](s => Try(UInt64Value(s.toLong)))
    implicit val encoder: Encoder[UInt64Value] =
      deriveEncoder[String, UInt64Value](_.value.toString)
  }

  private def deriveDecoder[Base, T](f: Base => Try[T])(implicit
      d: Decoder[Base]
  ): Decoder[T] =
    d.emapTry(f)

  private def deriveEncoder[Base, T](f: T => Base)(implicit
      e: Encoder[Base]
  ): Encoder[T] =
    e.contramap(f)

  object Converters {

    // Frequently BigQuery includes this note on an Int64Value time:
    // `Creation time of this job, in milliseconds since the epoch`
    def instantFromTime(time: Int64Value): Instant =
      Instant.ofEpochMilli(time.value)

  }

}
