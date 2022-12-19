package com.permutive.google.bigquery.rest.models.api.job.statistics

import cats.data.NonEmptyList
import com.permutive.google.bigquery.rest.models.api.TypeFormat.Int64Value
import com.permutive.google.bigquery.rest.models.api.{SchemaApi, TableReferenceApi}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/Job#JobStatistics2

sealed private[rest] trait QueryStatisticsApi {
  def cacheHit: Boolean
  def statementType: String
  def totalBytesProcessed: Int64Value
  def totalBytesBilled: Int64Value
  def referencedTables: Option[NonEmptyList[TableReferenceApi]]
}

private[rest] object QueryStatisticsApi {
  implicit val decoder: Decoder[QueryStatisticsApi] =
    DryQueryStatisticsApi.decoder.map(identity)

  implicit val encoder: Encoder.AsObject[QueryStatisticsApi] = { case d: DryQueryStatisticsApi =>
    DryQueryStatisticsApi.encoder.encodeObject(d)
  }
}

final private[rest] case class DryQueryStatisticsApi(
  totalBytesBilled: Int64Value,
  totalBytesProcessed: Int64Value,
  totalBytesProcessedAccuracy: BytesProcessedAccuracy,
  schema: Option[SchemaApi],
  cacheHit: Boolean,
  statementType: String,
  referencedTables: Option[NonEmptyList[TableReferenceApi]],
) extends QueryStatisticsApi

private[rest] object DryQueryStatisticsApi {
  implicit val decoder: Decoder[DryQueryStatisticsApi]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[DryQueryStatisticsApi] = deriveEncoder
}
