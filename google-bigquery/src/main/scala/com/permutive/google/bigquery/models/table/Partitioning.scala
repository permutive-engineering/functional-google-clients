/*
 * Copyright 2022 Permutive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.permutive.google.bigquery.models.table

import io.circe.{Decoder, Encoder, Json}

import scala.concurrent.duration.{Duration, _}

/** Represents how a BigQuery table is partitioned.
  *
  * API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/tables#TimePartitioning
  *
  * @param `type`
  *   [[PartitioningType]] which indicates the time-scale a table is partitioned over.
  * @param field
  *   Which field a table is partitioned over. Pseudo-column `_PARTITIONTIME` used if not set.
  * @param expiration
  *   The TTL for the partition. None for indefinite.
  */
sealed abstract class Partitioning private (
    val `type`: PartitioningType,
    val field: Option[Field.Name],
    val expiration: Option[Duration]
)

object Partitioning {
  def apply(`type`: PartitioningType, field: Option[Field.Name], expiration: Option[Duration]): Partitioning =
    new Partitioning(
      `type`,
      field,
      expiration
    ) {}

  implicit val msDurationDecoder: Decoder[Duration] =
    Decoder.instance(_.as[Long].map(_.millis))
  implicit val msDurationEncoder: Encoder[Duration] =
    Encoder.instance(duration => Json.fromLong(duration.toMillis))
  implicit val decoder: Decoder[Partitioning] =
    Decoder.forProduct3[Partitioning, PartitioningType, Option[
      Field.Name
    ], Option[Duration]](
      "type",
      "field",
      "expirationMs"
    )(Partitioning.apply)
  implicit val encoder: Encoder.AsObject[Partitioning] = Encoder
    .forProduct3[Partitioning, PartitioningType, Option[Field.Name], Option[
      Duration
    ]]("type", "field", "expirationMs")(p => (p.`type`, p.field, p.expiration))
}
