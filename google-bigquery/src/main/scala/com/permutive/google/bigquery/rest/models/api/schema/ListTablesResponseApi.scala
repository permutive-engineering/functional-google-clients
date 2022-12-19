package com.permutive.google.bigquery.rest.models.api.schema

import java.time.Instant

import com.permutive.google.bigquery.models.table.Partitioning
import com.permutive.google.bigquery.rest.models.api.{TableReferenceApi, TypeFormat}
import com.permutive.google.bigquery.rest.models.api.TypeFormat.Int64Value
import com.permutive.google.bigquery.rest.models.job.results.NewTypes.PageToken
import enumeratum.EnumEntry.Uppercase
import enumeratum.{CirceEnum, Enum, EnumEntry}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

import scala.collection.immutable

// API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/tables/list

private[rest] case class ListTablesResponseApi(
  tables: Option[List[ListTableResponseApi]],
  kind: String,
  etag: String,
  nextPageToken: Option[PageToken],
  totalItems: Int,
)
private[rest] object ListTablesResponseApi {
  implicit val decoder: Decoder[ListTablesResponseApi] = deriveDecoder
}

private[rest] case class ListTableResponseApi(
  kind: String,
  id: String,
  tableReference: TableReferenceApi,
  `type`: TableObjectType,
  timePartitioning: Option[Partitioning],
  view: Option[ViewDetailsApi],
  creationTime: Int64Value,
  expirationTime: Option[Int64Value],
) {

  val creationTimeInstant: Instant           = TypeFormat.Converters.instantFromTime(creationTime)
  val expirationTimeInstant: Option[Instant] = expirationTime.map(TypeFormat.Converters.instantFromTime)
}
private[rest] object ListTableResponseApi {
  implicit val decoder: Decoder[ListTableResponseApi] = deriveDecoder
}

private[rest] case class ViewDetailsApi(
  useLegacySql: Boolean,
)
private[rest] object ViewDetailsApi {
  implicit val decoder: Decoder[ViewDetailsApi] = deriveDecoder
}

sealed private[rest] trait TableObjectType extends EnumEntry with Uppercase

private[rest] object TableObjectType extends Enum[TableObjectType] with CirceEnum[TableObjectType] {
  override val values: immutable.IndexedSeq[TableObjectType] = findValues

  case object Table extends TableObjectType
  case object View  extends TableObjectType
}
