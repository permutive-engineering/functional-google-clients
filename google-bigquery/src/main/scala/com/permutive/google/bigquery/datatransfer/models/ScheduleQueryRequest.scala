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
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models.WriteDisposition
import com.permutive.google.bigquery.models.table.Field
import com.permutive.google.bigquery.models.table.NewTypes._

sealed abstract class ScheduleQueryRequest private (
    val displayName: DisplayName,
    val query: Query,
    val schedule: Schedule,
    val destinationDataset: DatasetId,
    val destinationTableName: TableId,
    val writeDisposition: WriteDisposition,
    val partitioningFieldName: Option[Field.Name]
)

object ScheduleQueryRequest {
  def apply(
      displayName: DisplayName,
      query: Query,
      schedule: Schedule,
      destinationDataset: DatasetId,
      destinationTableName: TableId,
      writeDisposition: WriteDisposition,
      partitioningFieldName: Option[Field.Name]
  ): ScheduleQueryRequest = new ScheduleQueryRequest(
    displayName,
    query,
    schedule,
    destinationDataset,
    destinationTableName,
    writeDisposition,
    partitioningFieldName
  ) {}

}
