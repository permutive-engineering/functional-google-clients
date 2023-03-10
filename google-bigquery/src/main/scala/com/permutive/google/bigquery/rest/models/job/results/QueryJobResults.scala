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

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.syntax.all._
import com.permutive.google.bigquery.models.NewTypes.Location
import com.permutive.google.bigquery.models.table.Field
import com.permutive.google.bigquery.rest.models.Cost
import com.permutive.google.bigquery.rest.models.Exceptions.MissingFieldsException
import com.permutive.google.bigquery.rest.models.api.TypeFormat.Int64Value
import com.permutive.google.bigquery.rest.models.api.job.JobQueryResultApi
import com.permutive.google.bigquery.rest.models.api.{ErrorProtoApi, SchemaApi}
import com.permutive.google.bigquery.rest.models.job.JobError
import com.permutive.google.bigquery.rest.models.job.results.NewTypes.{JobResultRow, PageToken}

sealed trait QueryJobResults {
  def errors: Option[
    NonEmptyList[JobError]
  ] // Errors do no necessarily indicate failure
}

sealed abstract class IncompleteJob private (
    val errors: Option[NonEmptyList[JobError]]
) extends QueryJobResults

object IncompleteJob {
  def apply(errors: Option[NonEmptyList[JobError]]): IncompleteJob = new IncompleteJob(errors) {}
}

sealed trait CompleteJob extends QueryJobResults {
  def schema: NonEmptyList[Field]
  def location: Option[Location]
  def totalBytesProcessed: Long
  def cacheHit: Boolean

  def cost: Cost = Cost(totalBytesProcessed, location)
}

sealed abstract class CompleteDmlJob private (
    val schema: NonEmptyList[Field],
    val location: Option[Location],
    val totalBytesProcessed: Long,
    val cacheHit: Boolean,
    val affectedRows: Long,
    val errors: Option[NonEmptyList[JobError]]
) extends CompleteJob {
  override val cost = Cost(totalBytesProcessed, location)
}

object CompleteDmlJob {
  def apply(
      schema: NonEmptyList[Field],
      location: Option[Location],
      totalBytesProcessed: Long,
      cacheHit: Boolean,
      affectedRows: Long,
      errors: Option[NonEmptyList[JobError]]
  ): CompleteDmlJob = new CompleteDmlJob(schema, location, totalBytesProcessed, cacheHit, affectedRows, errors) {}
}

sealed abstract class CompleteSelectJob private (
    val schema: NonEmptyList[Field],
    val location: Option[Location],
    val rows: Option[NonEmptyList[JobResultRow]],
    val totalRows: Long,
    val nextPageToken: Option[PageToken],
    val totalBytesProcessed: Long,
    val cacheHit: Boolean,
    val errors: Option[NonEmptyList[JobError]]
) extends CompleteJob {
  override val cost = Cost(totalBytesProcessed, location)
}

object CompleteSelectJob {
  def apply(
      schema: NonEmptyList[Field],
      location: Option[Location],
      rows: Option[NonEmptyList[JobResultRow]],
      totalRows: Long,
      nextPageToken: Option[PageToken],
      totalBytesProcessed: Long,
      cacheHit: Boolean,
      errors: Option[NonEmptyList[JobError]]
  ): CompleteSelectJob =
    new CompleteSelectJob(schema, location, rows, totalRows, nextPageToken, totalBytesProcessed, cacheHit, errors) {}

}

object QueryJobResults {

  private[rest] def fromResponse(
      api: JobQueryResultApi
  ): Either[MissingFieldsException, QueryJobResults] =
    if (api.jobComplete) fromCompleteResponse(api)
    else Right(IncompleteJob(convertErrors(api.errors)))

  private def fromCompleteResponse(
      api: JobQueryResultApi
  ): Either[MissingFieldsException, CompleteJob] =
    api.numDmlAffectedRows
      .fold[Validated[MissingFieldsException, CompleteJob]](
        completeSelect(api)
      )(completeDml(api))
      .toEither

  private def completeSelect(
      api: JobQueryResultApi
  ): Validated[MissingFieldsException, CompleteSelectJob] =
    (
      extractSchema(api),
      extractOption(api.totalRows, "totalRows"),
      extractTotalBytes(api),
      extractCacheHit(api)
    ).mapN { case (schema, totalRows, bytes, cacheHit) =>
      CompleteSelectJob(
        schema.fields,
        api.jobReference.location,
        api.rows.flatMap(rows => NonEmptyList.fromList(rows.map(JobResultRow(_)))),
        totalRows.value,
        api.pageToken,
        bytes.value,
        cacheHit,
        convertErrors(api.errors)
      )
    }.leftMap(
      MissingFieldsException(
        "convert BigQuery job result to CompleteSelectJob",
        _
      )
    )

  private def completeDml(
      api: JobQueryResultApi
  )(nRows: Int64Value): Validated[MissingFieldsException, CompleteDmlJob] =
    (
      extractSchema(api),
      extractTotalBytes(api),
      extractCacheHit(api)
    ).mapN { case (schema, bytes, cacheHit) =>
      CompleteDmlJob(
        schema.fields,
        api.jobReference.location,
        bytes.value,
        cacheHit,
        nRows.value,
        convertErrors(api.errors)
      )
    }.leftMap(
      MissingFieldsException("convert BigQuery job result to CompleteDmlJob", _)
    )

  private def extractCacheHit(
      api: JobQueryResultApi
  ): ValidatedNel[String, Boolean] =
    extractOption(api.cacheHit, "cacheHit")

  private def extractTotalBytes(
      api: JobQueryResultApi
  ): ValidatedNel[String, Int64Value] =
    extractOption(api.totalBytesProcessed, "totalBytesProcessed")

  private def extractSchema(
      api: JobQueryResultApi
  ): ValidatedNel[String, SchemaApi] =
    extractOption(api.schema, "schema")

  private def extractOption[T](
      o: Option[T],
      fieldName: String
  ): ValidatedNel[String, T] =
    Validated.fromOption(o, NonEmptyList.one(fieldName))

  private def convertErrors(
      errorsO: Option[List[ErrorProtoApi]]
  ): Option[NonEmptyList[JobError]] =
    errorsO.flatMap(JobError.many)

}
