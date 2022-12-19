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

import cats.data.NonEmptyList
import com.permutive.google.bigquery.datatransfer.models.NewTypes._
import com.permutive.google.bigquery.models.Exceptions.BigQueryException
import com.permutive.google.bigquery.models.NewTypes._

object Exceptions {

  case class DuplicateScheduledQueryRequestException(
      identities: NonEmptyList[ScheduledQueryIdentity]
  ) extends RuntimeException(
        s"Attempted to schedule queries but duplicate queries by identity are present in the request: ${identities.toList}"
      )
      with BigQueryException

  case class ScheduledQueryExistsException(
      project: BigQueryProjectName,
      location: Location,
      displayName: DisplayName,
      datasetId: DatasetId,
      configIds: NonEmptyList[ConfigId]
  ) extends RuntimeException(
        s"Attempted to create a scheduled query but scheduled queries with the name `$displayName` in dataset `$datasetId`; location `$location`; project `$project` already exist. Config ID(s): ${configIds.toList}"
      )
      with BigQueryException

  case class ScheduledQueriesExistException(
      project: BigQueryProjectName,
      location: Location,
      identities: NonEmptyList[ScheduledQueryIdentity]
  ) extends RuntimeException(
        s"Attempted to create scheduled queries but scheduled queries with the identities ${identities.toList}; location `$location`; project `$project` already exist."
      )
      with BigQueryException

}
