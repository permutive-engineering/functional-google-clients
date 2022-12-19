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

package com.permutive.google.bigquery.rest.models.job.results

import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.NewTypes.Location
import com.permutive.google.bigquery.models.table.Field
import com.permutive.google.bigquery.rest.models.Cost
import com.permutive.google.bigquery.rest.models.job.JobError
import io.scalaland.chimney.dsl._

case class SelectJobMetadata(
    schema: NonEmptyList[Field],
    totalRows: Long,
    location: Option[Location],
    totalBytesProcessed: Long,
    cost: Cost,
    cacheHit: Boolean,
    errors: Option[NonEmptyList[JobError]]
)

object SelectJobMetadata {

  def fromCompleteSelectJob(
      completeSelectJob: CompleteSelectJob
  ): SelectJobMetadata =
    completeSelectJob.transformInto[SelectJobMetadata]

}
