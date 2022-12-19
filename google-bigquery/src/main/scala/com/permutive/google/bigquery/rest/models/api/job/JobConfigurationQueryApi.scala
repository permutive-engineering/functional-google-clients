package com.permutive.google.bigquery.rest.models.api.job

import cats.data.NonEmptyList
import cats.syntax.all._
import com.permutive.google.bigquery.models.NewTypes.Query
import com.permutive.google.bigquery.models.WriteDisposition
import com.permutive.google.bigquery.rest.models.api.TableReferenceApi
import com.permutive.google.bigquery.rest.models.job.queryparameters.QueryParameter
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax._
import io.circe.{Decoder, Encoder}

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/JobConfiguration#JobConfigurationQuery

sealed private[rest] trait JobConfigurationQueryApi {
  def query: Query
  def useLegacySql: Boolean
}

object JobConfigurationQueryApi {

  implicit val encoder: Encoder.AsObject[JobConfigurationQueryApi] = {
    case writeTable: JobConfigurationQueryWriteTableApi => writeTable.asJsonObject
    case basic: JobConfigurationQueryBasicApi           => basic.asJsonObject
  }

  // Want to decode the most specific that we can first
  implicit val decoder: Decoder[JobConfigurationQueryApi] =
    List[Decoder[JobConfigurationQueryApi]](
      JobConfigurationQueryWriteTableApi.decoder.widen,
      JobConfigurationQueryBasicApi.decoder.widen,
    ).reduceLeft(_.or(_))
}

final private[rest] case class JobConfigurationQueryBasicApi(
  query: Query,
  useLegacySql: Boolean,
  queryParameters: Option[NonEmptyList[QueryParameter]],
) extends JobConfigurationQueryApi

private[rest] object JobConfigurationQueryBasicApi {
  implicit val encoder: Encoder.AsObject[JobConfigurationQueryBasicApi] = deriveEncoder
  implicit val decoder: Decoder[JobConfigurationQueryBasicApi]          = deriveDecoder
}

final private[rest] case class JobConfigurationQueryWriteTableApi(
  query: Query,
  writeDisposition: WriteDisposition,
  destinationTable: TableReferenceApi,
  useLegacySql: Boolean,
) extends JobConfigurationQueryApi

private[rest] object JobConfigurationQueryWriteTableApi {
  implicit val encoder: Encoder.AsObject[JobConfigurationQueryWriteTableApi] = deriveEncoder
  implicit val decoder: Decoder[JobConfigurationQueryWriteTableApi]          = deriveDecoder
}
