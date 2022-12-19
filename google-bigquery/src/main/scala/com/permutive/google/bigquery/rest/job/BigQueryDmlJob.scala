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

import cats.syntax.all._
import cats.{Applicative, MonadError}
import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.rest.models.Exceptions._
import com.permutive.google.bigquery.rest.models.job.NewTypes._
import com.permutive.google.bigquery.rest.models.job.PollSettings
import com.permutive.google.bigquery.rest.models.job.queryparameters.QueryParameter
import com.permutive.google.bigquery.rest.models.job.results.{
  CompleteDmlJob,
  QueryJobResults
}

trait BigQueryDmlJob[F[_]] {

  def createRunDmlQuery(
      jobId: Option[JobId],
      query: Query,
      legacySql: Boolean = false,
      location: Option[Location] = None,
      pollSettings: PollSettings = PollSettings.default,
      queryParameters: Option[NonEmptyList[QueryParameter]] = None
  ): F[CompleteDmlJob]

}

object BigQueryDmlJob {

  def apply[F[_]: BigQueryDmlJob]: BigQueryDmlJob[F] = implicitly

  def create[F[_]: BigQueryJob: MonadError[*[_], Throwable]]
      : F[BigQueryDmlJob[F]] =
    Applicative[F].pure(impl)

  def impl[F[_]: BigQueryJob](implicit
      F: MonadError[F, Throwable]
  ): BigQueryDmlJob[F] =
    new BigQueryDmlJob[F] {

      override def createRunDmlQuery(
          jobId: Option[JobId],
          query: Query,
          legacySql: Boolean = false,
          location: Option[Location] = None,
          pollSettings: PollSettings = PollSettings.default,
          queryParameters: Option[NonEmptyList[QueryParameter]] = None
      ): F[CompleteDmlJob] =
        for {
          job <- BigQueryJob[F].createQueryJobPollSuccessful(
            jobId,
            query,
            legacySql,
            location,
            pollSettings,
            queryParameters
          )
          qryRes <- BigQueryJob[F].getQueryJobResults(
            job.id,
            location,
            None,
            None
          )
          dmlRes <- raiseIfNotDmlResults(qryRes, job.id)
        } yield dmlRes

      private def raiseIfNotDmlResults(
          res: QueryJobResults,
          jobId: JobId
      ): F[CompleteDmlJob] =
        res match {
          case dml: CompleteDmlJob => dml.pure
          case _ => InvalidResultForDmlException(res, jobId).raiseError
        }

    }

}
