package com.permutive.google.bigquery.models.table

import io.circe.{Decoder, Encoder, Json}

import scala.concurrent.duration.{Duration, _}

/**
  * Represents how a BigQuery table is partitioned.
  *
  * API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/tables#TimePartitioning
  *
  * @param `type` [[PartitioningType]] which indicates the time-scale a table is partitioned over.
  * @param field Which field a table is partitioned over. Pseudo-column `_PARTITIONTIME` used if not set.
  * @param expiration The TTL for the partition. None for indefinite.
  */
case class Partitioning(`type`: PartitioningType, field: Option[Field.Name], expiration: Option[Duration] = None)

object Partitioning {
  implicit val msDurationDecoder: Decoder[Duration] = Decoder.instance(_.as[Long].map(_.millis))
  implicit val msDurationEncoder: Encoder[Duration] = Encoder.instance(duration => Json.fromLong(duration.toMillis))
  implicit val decoder: Decoder[Partitioning] =
    Decoder.forProduct3[Partitioning, PartitioningType, Option[Field.Name], Option[Duration]](
      "type",
      "field",
      "expirationMs"
    )(Partitioning.apply)
  implicit val encoder: Encoder.AsObject[Partitioning] = Encoder
    .forProduct3[Partitioning, PartitioningType, Option[Field.Name], Option[Duration]]("type", "field", "expirationMs")(
      p => (p.`type`, p.field, p.expiration)
    )
}
