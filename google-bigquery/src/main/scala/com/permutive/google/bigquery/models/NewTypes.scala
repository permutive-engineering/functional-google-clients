package com.permutive.google.bigquery.models

import cats.Eq
import io.circe.{Decoder, Encoder}
import io.estatico.newtype.macros.newtype

object NewTypes {

  @newtype case class DatasetId(value: String)
  object DatasetId {
    implicit val encoder: Encoder[DatasetId] = deriving
    implicit val decoder: Decoder[DatasetId] = deriving

    implicit val eq: Eq[DatasetId] = Eq.fromUniversalEquals
  }

  @newtype case class PermutiveProjectUniqueName(value: String)
  object PermutiveProjectUniqueName {
    implicit val encoder: Encoder[PermutiveProjectUniqueName] = deriving
    implicit val decoder: Decoder[PermutiveProjectUniqueName] = deriving
  }

  // Permutive's project name in BQ (e.g. permutive-11111), *NOT* management project name
  @newtype case class BigQueryProjectName(value: String)
  object BigQueryProjectName {
    implicit val encoder: Encoder[BigQueryProjectName] = deriving
    implicit val decoder: Decoder[BigQueryProjectName] = deriving
  }

  // Permutive's ID that corresponds to a bigquery project name, seems to be used internally
  @newtype case class BigQueryProjectId(value: String)
  object BigQueryProjectId {
    implicit val encoder: Encoder[BigQueryProjectId] = deriving
    implicit val decoder: Decoder[BigQueryProjectId] = deriving
  }
  @newtype case class Query(value: String)
  object Query {
    implicit val encoder: Encoder[Query] = deriving
    implicit val decoder: Decoder[Query] = deriving
  }

  @newtype case class Location(value: String)
  object Location {
    implicit val encoder: Encoder[Location] = deriving
    implicit val decoder: Decoder[Location] = deriving

    // Documentation: https://cloud.google.com/bigquery/docs/locations

    val US = Location("us")
    val EU = Location("eu")

    val USWest2    = Location("us-west2")
    val LosAngeles = USWest2

    val NorthAmericaNorthEast1 = Location("northamerica-northeast1")
    val Montreal               = NorthAmericaNorthEast1

    val UsEast4          = Location("us-east4")
    val NorthernVirginia = UsEast4

    val SouthAmericaEast1 = Location("southamerica-east1")
    val SaoPaulo          = SouthAmericaEast1

    val EuropeNorth1 = Location("europe-north1")
    val Finland      = EuropeNorth1

    val EuropeWest2 = Location("europe-west2")
    val London      = EuropeWest2

    val EuropeWest6 = Location("europe-west6")
    val Zurich      = EuropeWest6

    val AsiaEast2 = Location("asia-east2")
    val HongKong  = AsiaEast2

    val AsiaSouth1 = Location("asia-south1")
    val Mumbai     = AsiaSouth1

    val AsiaNorthEast2 = Location("asia-northeast2")
    val Osaka          = AsiaNorthEast2

    val AsiaEast1 = Location("asia-east1")
    val Taiwan    = AsiaEast1

    val AsiaNorthEast1 = Location("asia-northeast1")
    val Tokyo          = AsiaNorthEast1

    val AsiaSouthEast1 = Location("asia-southeast1")
    val Singapore      = AsiaSouthEast1

    val AustraliaSouthEast1 = Location("australia-southeast1")
    val Sydney              = AustraliaSouthEast1
  }

}
