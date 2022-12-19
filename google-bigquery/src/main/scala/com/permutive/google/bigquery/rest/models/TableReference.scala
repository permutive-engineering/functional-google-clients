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

package com.permutive.google.bigquery.rest.models

import com.permutive.google.bigquery.models.NewTypes.{
  BigQueryProjectName,
  DatasetId
}
import com.permutive.google.bigquery.models.table.NewTypes.TableId
import com.permutive.google.bigquery.rest.models.api.TableReferenceApi
import io.scalaland.chimney.dsl._

case class TableReference(
    projectId: BigQueryProjectName,
    dataset: DatasetId,
    name: TableId
)

object TableReference {

  private[rest] def fromApi(api: TableReferenceApi): TableReference =
    api
      .into[TableReference]
      .withFieldRenamed(_.datasetId, _.dataset)
      .withFieldRenamed(_.tableId, _.name)
      .transform

}
