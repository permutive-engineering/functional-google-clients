package com.permutive.google.bigquery.rest.schema

import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.NewTypes.{DatasetId, Location, Query}
import com.permutive.google.bigquery.models.schema.Access
import com.permutive.google.bigquery.models.table.NewTypes.TableId
import com.permutive.google.bigquery.models.table.{Field, Partitioning}
import com.permutive.google.bigquery.rest.models.job.PaginationSettings
import com.permutive.google.bigquery.rest.models.job.results.NewTypes.PageToken
import com.permutive.google.bigquery.rest.models.schema.{
  DatasetObject,
  ListTablesAndViewsResult,
  TablesAndViewsMetadata,
}
import com.permutive.google.bigquery.rest.utils.StreamUtils
import fs2.Stream

trait BigQuerySchema[F[_]] {

  def createDataset(
    datasetId: DatasetId,
    location: Location,
    labels: Option[Map[String, String]],
  ): F[Unit]

  def createDataset(
    datasetId: DatasetId,
    location: Location,
    access: NonEmptyList[Access],
    labels: Option[Map[String, String]],
  ): F[Unit]

  def createTable(
    name: TableId,
    dataset: DatasetId,
    fields: NonEmptyList[Field],
    partitioning: Option[Partitioning],
  ): F[Unit]

  // https://cloud.google.com/bigquery/docs/reference/rest/v2/tables/patch
  // note the new fields must be compatible with the permitted modifications described here:
  //  https://cloud.google.com/bigquery/docs/managing-table-schemas
  def patchTable(
    tableId: TableId,
    datasetId: DatasetId,
    fields: NonEmptyList[Field]
  ): F[Unit]

  def createView(
    name: TableId,
    dataset: DatasetId,
    query: Query,
    legacySql: Boolean = false,
  ): F[Unit]

  def listTablesAndViews(
    dataset: DatasetId,
    maxResults: Option[Int] = None,
    pageToken: Option[PageToken] = None,
  ): F[ListTablesAndViewsResult]

  def listAllTablesAndViews(
    dataset: DatasetId,
    paginationSettings: PaginationSettings = PaginationSettings.default,
  ): F[(TablesAndViewsMetadata, Stream[F, DatasetObject])]

  def listAllTablesAndViewsStream(
    dataset: DatasetId,
    paginationSettings: PaginationSettings = PaginationSettings.default,
  ): Stream[F, DatasetObject] =
    StreamUtils.flattenUnrolled(listAllTablesAndViews(dataset, paginationSettings))
}

object BigQuerySchema {
  def apply[F[_]: BigQuerySchema]: BigQuerySchema[F] = implicitly
}
