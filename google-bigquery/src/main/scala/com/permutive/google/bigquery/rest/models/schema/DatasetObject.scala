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

package com.permutive.google.bigquery.rest.models.schema

import java.time.Instant

import cats.data.NonEmptyList
import cats.syntax.all._
import com.permutive.google.bigquery.models.Exceptions.BigQueryException
import com.permutive.google.bigquery.models.NewTypes.DatasetId
import com.permutive.google.bigquery.models.table.NewTypes.TableId
import com.permutive.google.bigquery.models.table.Partitioning
import com.permutive.google.bigquery.rest.models.Exceptions.MissingFieldsException
import com.permutive.google.bigquery.rest.models.api.schema.{ListTableResponseApi, TableObjectType}

sealed trait DatasetObject {
  def name: TableId
  def dataset: DatasetId
  def creationTime: Instant
  def expirationTime: Option[Instant]
}

object DatasetObject {

  private[rest] def fromResponse(
      response: ListTableResponseApi
  ): Either[BigQueryException, DatasetObject] =
    response.`type` match {
      case TableObjectType.Table => readTable(response)
      case TableObjectType.View => readView(response)
    }

  private def readTable(
      responseApi: ListTableResponseApi
  ): Either[BigQueryException, Table] =
    Right(
      new Table(
        responseApi.tableReference.tableId,
        responseApi.tableReference.datasetId,
        responseApi.timePartitioning,
        responseApi.creationTimeInstant,
        responseApi.expirationTimeInstant
      )
    )

  private def readView(
      responseApi: ListTableResponseApi
  ): Either[BigQueryException, View] = {
    val legacySqlEith: Either[MissingFieldsException, Boolean] =
      responseApi.view
        .map(_.useLegacySql.asRight)
        .getOrElse(
          MissingFieldsException(
            s"convert BigQuery table response of type ${TableObjectType.View} to View",
            NonEmptyList.one("view")
          ).asLeft
        )

    legacySqlEith.map { legacySql =>
      View(
        responseApi.tableReference.tableId,
        responseApi.tableReference.datasetId,
        legacySql,
        responseApi.creationTimeInstant,
        responseApi.expirationTimeInstant
      )
    }
  }

}

final class Table(
    override val name: TableId,
    override val dataset: DatasetId,
    val partitioning: Option[Partitioning],
    override val creationTime: Instant,
    override val expirationTime: Option[Instant]
) extends DatasetObject

final case class View(
    name: TableId,
    dataset: DatasetId,
    legacySql: Boolean,
    creationTime: Instant,
    expirationTime: Option[Instant]
) extends DatasetObject
