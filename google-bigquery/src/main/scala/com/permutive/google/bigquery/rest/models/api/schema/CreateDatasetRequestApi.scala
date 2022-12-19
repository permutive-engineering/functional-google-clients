package com.permutive.google.bigquery.rest.models.api.schema

import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.NewTypes.{BigQueryProjectName, DatasetId, Location}
import com.permutive.google.bigquery.models.schema.Access
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// https://cloud.google.com/bigquery/docs/reference/rest/v2/datasets#Dataset

private[rest] case class CreateDatasetRequestApi(
  datasetReference: DatasetReferenceApi,
  location: Location,
  access: Option[NonEmptyList[Access]],
  labels: Option[Map[String, String]],
)

private[rest] case class CreateDatasetResponseApi(
  kind: String,
  etag: String,
  id: String,
  selfLink: String,
  datasetReference: DatasetReferenceApi,
  location: Location,
  access: Option[NonEmptyList[Access]],
  // and more...
)

private[rest] object CreateDatasetResponseApi {
  implicit val decoder: Decoder[CreateDatasetResponseApi] = deriveDecoder
}

private[rest] case class DatasetReferenceApi(
  datasetId: DatasetId,
  projectId: BigQueryProjectName,
)

private[rest] object DatasetReferenceApi {
  implicit val decoder: Decoder[DatasetReferenceApi]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[DatasetReferenceApi] = deriveEncoder
}

private[rest] object CreateDatasetRequestApi {
  implicit val encoder: Encoder.AsObject[CreateDatasetRequestApi] = deriveEncoder
}
