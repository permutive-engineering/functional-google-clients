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

package com.permutive.google.bigquery.models

import cats.Eq
import io.circe.{Decoder, Encoder}

object NewTypes {

  case class DatasetId(value: String) extends AnyVal
  object DatasetId {
    implicit val encoder: Encoder[DatasetId] =
      Encoder.encodeString.contramap(_.value)
    implicit val decoder: Decoder[DatasetId] =
      Decoder.decodeString.map(DatasetId(_))

    implicit val eq: Eq[DatasetId] = Eq.fromUniversalEquals
  }

  case class PermutiveProjectUniqueName(value: String) extends AnyVal
  object PermutiveProjectUniqueName {
    implicit val encoder: Encoder[PermutiveProjectUniqueName] =
      Encoder.encodeString.contramap(_.value)

    implicit val decoder: Decoder[PermutiveProjectUniqueName] =
      Decoder.decodeString.map(PermutiveProjectUniqueName(_))
  }

  case class BigQueryProjectName(value: String) extends AnyVal
  object BigQueryProjectName {
    implicit val encoder: Encoder[BigQueryProjectName] =
      Encoder.encodeString.contramap(_.value)

    implicit val decoder: Decoder[BigQueryProjectName] =
      Decoder.decodeString.map(BigQueryProjectName(_))
  }

  case class BigQueryProjectId(value: String) extends AnyVal
  object BigQueryProjectId {
    implicit val encoder: Encoder[BigQueryProjectId] =
      Encoder.encodeString.contramap(_.value)

    implicit val decoder: Decoder[BigQueryProjectId] =
      Decoder.decodeString.map(BigQueryProjectId(_))
  }
  case class Query(value: String) extends AnyVal
  object Query {
    implicit val encoder: Encoder[Query] =
      Encoder.encodeString.contramap(_.value)

    implicit val decoder: Decoder[Query] = Decoder.decodeString.map(Query(_))
  }

  case class Location(value: String) extends AnyVal
  object Location {
    implicit val encoder: Encoder[Location] =
      Encoder.encodeString.contramap(_.value)

    implicit val decoder: Decoder[Location] =
      Decoder.decodeString.map(Location(_))

    // Documentation: https://cloud.google.com/bigquery/docs/locations

    val US = Location("us")
    val EU = Location("eu")

    val USWest2 = Location("us-west2")
    val LosAngeles = USWest2

    val NorthAmericaNorthEast1 = Location("northamerica-northeast1")
    val Montreal = NorthAmericaNorthEast1

    val UsEast4 = Location("us-east4")
    val NorthernVirginia = UsEast4

    val SouthAmericaEast1 = Location("southamerica-east1")
    val SaoPaulo = SouthAmericaEast1

    val EuropeNorth1 = Location("europe-north1")
    val Finland = EuropeNorth1

    val EuropeWest2 = Location("europe-west2")
    val London = EuropeWest2

    val EuropeWest6 = Location("europe-west6")
    val Zurich = EuropeWest6

    val AsiaEast2 = Location("asia-east2")
    val HongKong = AsiaEast2

    val AsiaSouth1 = Location("asia-south1")
    val Mumbai = AsiaSouth1

    val AsiaNorthEast2 = Location("asia-northeast2")
    val Osaka = AsiaNorthEast2

    val AsiaEast1 = Location("asia-east1")
    val Taiwan = AsiaEast1

    val AsiaNorthEast1 = Location("asia-northeast1")
    val Tokyo = AsiaNorthEast1

    val AsiaSouthEast1 = Location("asia-southeast1")
    val Singapore = AsiaSouthEast1

    val AustraliaSouthEast1 = Location("australia-southeast1")
    val Sydney = AustraliaSouthEast1
  }

}
