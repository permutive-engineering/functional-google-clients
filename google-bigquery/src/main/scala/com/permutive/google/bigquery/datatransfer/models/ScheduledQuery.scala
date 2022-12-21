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

package com.permutive.google.bigquery.datatransfer.models

import com.permutive.google.bigquery.datatransfer.models.NewTypes._
import com.permutive.google.bigquery.datatransfer.models.api._
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models.WriteDisposition
import com.permutive.google.bigquery.models.table.Field
import com.permutive.google.bigquery.models.table.NewTypes._

sealed abstract class ScheduledQuery private (
    val configId: ConfigId,
    val displayName: DisplayName,
    val query: Query,
    val schedule: Schedule,
    val destinationDataset: DatasetId,
    val destinationTableName: Option[TableId],
    val writeDisposition: Option[WriteDisposition],
    val partitioningFieldName: Option[Field.Name]
)

object ScheduledQuery {

  def apply(
      configId: ConfigId,
      displayName: DisplayName,
      query: Query,
      schedule: Schedule,
      destinationDataset: DatasetId,
      destinationTableName: Option[TableId],
      writeDisposition: Option[WriteDisposition],
      partitioningFieldName: Option[Field.Name]
  ): ScheduledQuery = new ScheduledQuery(
    configId,
    displayName,
    query,
    schedule,
    destinationDataset,
    destinationTableName,
    writeDisposition,
    partitioningFieldName
  ) {}

  private[datatransfer] def fromApi(
      api: ScheduledQueryResponseApi
  ): ScheduledQuery =
    ScheduledQuery(
      api.name.configId,
      api.displayName,
      api.params.query,
      api.schedule,
      api.destinationDatasetId,
      api.params.`destination_table_name_template`,
      api.params.`write_disposition`,
      api.params.`partitioning_field`
    )

}
