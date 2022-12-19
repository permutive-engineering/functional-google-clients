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

package com.permutive.google.bigquery.rest.job

import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models.WriteDisposition
import com.permutive.google.bigquery.models.table.NewTypes._
import com.permutive.google.bigquery.rest.models.job.NewTypes._
import com.permutive.google.bigquery.rest.models.job._
import com.permutive.google.bigquery.rest.models.job.results.NewTypes.PageToken
import com.permutive.google.bigquery.rest.models.job.results.{
  DryRunQueryJob,
  QueryJobResults
}
import cats.data.NonEmptyList
import com.permutive.google.bigquery.rest.models.job.queryparameters.QueryParameter

trait BigQueryJob[F[_]] {

  def createQueryJob(
      jobId: Option[JobId],
      query: Query,
      legacySql: Boolean = false,
      location: Option[Location] = None,
      queryParameters: Option[NonEmptyList[QueryParameter]] = None
  ): F[Job]

  /** Dry-run a BigQuery query, returning statistics on the query.
    *
    * Does not require a jobId as BigQuery does not return them for dry-run
    * jobs.
    */
  def dryRunQuery(
      query: Query,
      legacySql: Boolean = false,
      location: Option[Location] = None,
      queryParameters: Option[NonEmptyList[QueryParameter]] = None
  ): F[DryRunQueryJob]

  def createQueryJobPollSuccessful(
      jobId: Option[JobId],
      query: Query,
      legacySql: Boolean = false,
      location: Option[Location] = None,
      pollSettings: PollSettings = PollSettings.default,
      queryParameters: Option[NonEmptyList[QueryParameter]] = None
  ): F[SuccessfulJob]

  def createQueryWriteTableJob(
      jobId: Option[JobId],
      query: Query,
      destinationDataset: DatasetId,
      destinationTable: TableId,
      writeDisposition: WriteDisposition,
      legacySql: Boolean = false,
      location: Option[Location] = None
  ): F[Job]

  def createQueryWriteTableJobPollSuccessful(
      jobId: Option[JobId],
      query: Query,
      destinationDataset: DatasetId,
      destinationTable: TableId,
      writeDisposition: WriteDisposition,
      legacySql: Boolean = false,
      location: Option[Location] = None,
      pollSettings: PollSettings = PollSettings.default
  ): F[SuccessfulJob]

  def getQueryJobState(jobId: JobId, location: Option[Location] = None): F[Job]

  def pollJob(
      jobId: JobId,
      location: Option[Location] = None,
      pollSettings: PollSettings = PollSettings.default
  ): F[CompleteJob]

  def getQueryJobResults(
      jobId: JobId,
      location: Option[Location] = None,
      pageToken: Option[PageToken] = None,
      maxResults: Option[Int] = None
  ): F[QueryJobResults]

  def getQueryJobResults(
      jobId: JobId,
      pageToken: PageToken
  ): F[QueryJobResults] =
    getQueryJobResults(jobId, pageToken = Some(pageToken))

  def getQueryJobResults(jobId: JobId, maxResults: Int): F[QueryJobResults] =
    getQueryJobResults(jobId, maxResults = Some(maxResults))

}

object BigQueryJob {
  def apply[F[_]: BigQueryJob]: BigQueryJob[F] = implicitly
}
