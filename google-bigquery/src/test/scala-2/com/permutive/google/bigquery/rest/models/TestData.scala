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

import cats.data.NonEmptyList
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models._
import com.permutive.google.bigquery.models.table.NewTypes._
import com.permutive.google.bigquery.rest.models.api.TypeFormat._
import com.permutive.google.bigquery.rest.models.api._
import com.permutive.google.bigquery.rest.models.api.job._
import com.permutive.google.bigquery.rest.models.api.job.statistics.{
  BytesProcessedAccuracy,
  DryQueryStatisticsApi,
  DryRunQueryJobStatisticsApi
}
import com.permutive.google.bigquery.rest.models.job.JobState
import com.permutive.google.bigquery.rest.models.job.NewTypes._
import com.permutive.google.bigquery.rest.models.job.results.NewTypes.PageToken
import io.circe.Json
import com.permutive.google.bigquery.rest.models.job.queryparameters.{
  ListMapLike,
  QueryParameter,
  QueryParameterType,
  QueryParameterValue,
  StructType
}

object TestData {

  val bqProject = BigQueryProjectName("test-project")
  val bqDataset = DatasetId("test-dataset")
  val testTable = TableId("table")
  val testQuery = Query("SELECT * FROM foo WHERE bar = 1")
  val parametrisedTestQuery = Query("SELECT * FROM foo WHERE bar = @x")

  val writeDisposition = WriteDisposition.WriteTruncate

  val tableReference = TableReferenceApi(
    bqProject,
    bqDataset,
    testTable
  )

  val useLegactSql = false

  val jobQueryConfigWriteTableApi = JobConfigurationQueryWriteTableApi(
    testQuery,
    writeDisposition,
    tableReference,
    useLegacySql = useLegactSql
  )

  val jobQueryConfigBasicApi = JobConfigurationQueryBasicApi(
    testQuery,
    useLegacySql = useLegactSql,
    queryParameters = None
  )

  val jobId = JobId("my-job")
  val location = Location("us-1")

  val jobReferenceApi = JobReferenceApi(
    jobId,
    Some(location),
    bqProject
  )

  val jobConfigurationApiQueryWriteTable =
    JobConfigurationApi.Query(jobQueryConfigWriteTableApi)
  val jobConfigurationApiQueryWriteTableDryRun =
    jobConfigurationApiQueryWriteTable.copy(dryRun = Some(true))

  val jobConfigurationQueryBasicNoDryRun =
    JobConfigurationApi.Query(jobQueryConfigBasicApi)
  val jobConfigurationQueryBasicDryRun =
    jobConfigurationQueryBasicNoDryRun.copy(dryRun = Some(true))

  val createQueryWriteTableJobRequestApi = CreateQueryJobRequestApi(
    jobConfigurationApiQueryWriteTable,
    jobReferenceApi
  )

  val createQueryBasicJobRequestApiNoDryRun = CreateQueryJobRequestApi(
    jobConfigurationQueryBasicNoDryRun,
    jobReferenceApi
  )
  val createQueryBasicJobRequestApiDryRun =
    createQueryBasicJobRequestApiNoDryRun.copy(configuration = jobConfigurationQueryBasicDryRun)

  val createCopyJobRequestApi = CreateQueryJobRequestApi(
    JobConfigurationApi.Copy,
    jobReferenceApi
  )

  object Errors {
    val errorProtoApi1 = ErrorProtoApi(
      "reason-1",
      Some("location-1"),
      "message-1"
    )

    val errorProtoApi2 = ErrorProtoApi(
      "reason-2",
      Some("location-2"),
      "message-2"
    )

    val errorProtoApi3 = ErrorProtoApi(
      "reason-3",
      Some("location-3"),
      "message-3"
    )
  }

  object JobStatuses {
    val successfulJobStatus = JobStatusApi(
      JobState.Done,
      None,
      None
    )

    val failedJobStatus = JobStatusApi(
      JobState.Done,
      Some(Errors.errorProtoApi1),
      Some(List(Errors.errorProtoApi2, Errors.errorProtoApi3))
    )

    val failedJobStatusEmptyList = JobStatusApi(
      JobState.Done,
      Some(Errors.errorProtoApi1),
      Some(Nil)
    )

    val failedJobStatusNoList = JobStatusApi(
      JobState.Done,
      Some(Errors.errorProtoApi1),
      None
    )

    val completedJobStatusListNoError = JobStatusApi(
      JobState.Done,
      None,
      Some(List(Errors.errorProtoApi2, Errors.errorProtoApi3))
    )

    val runningJobStatus = JobStatusApi(
      JobState.Running,
      None,
      None
    )

    val pendingJobStatus = JobStatusApi(
      JobState.Pending,
      None,
      None
    )

  }

  val kind = "this-kind"
  val etag = "some-etag"

  object QueryJobResponses {
    val selfLink: Option[String] = None

    val successfulJobResponseApi = QueryJobResponseApi(
      JobStatuses.successfulJobStatus,
      jobReferenceApi,
      jobConfigurationApiQueryWriteTable,
      kind,
      etag,
      selfLink
    )

    val failedJobResponseApi = QueryJobResponseApi(
      JobStatuses.failedJobStatus,
      jobReferenceApi,
      jobConfigurationApiQueryWriteTable,
      kind,
      etag,
      selfLink
    )

    val runningJobResponseApi = QueryJobResponseApi(
      JobStatuses.runningJobStatus,
      jobReferenceApi,
      jobConfigurationApiQueryWriteTable,
      kind,
      etag,
      selfLink
    )

    val pendingJobResponseApi = QueryJobResponseApi(
      JobStatuses.pendingJobStatus,
      jobReferenceApi,
      jobConfigurationApiQueryWriteTable.copy(),
      kind,
      etag,
      selfLink
    )
  }

  object DryQueryStatistics {

    val dryQueryStatisticsWithReferenceAndSchema = DryQueryStatisticsApi(
      Int64Value(0L),
      Int64Value(1585515762L),
      BytesProcessedAccuracy.Precise,
      Some(SchemaApi(NonEmptyList.one(Fields.floatNullable))),
      cacheHit = false,
      "SELECT",
      Some(NonEmptyList.one(tableReference))
    )

  }

  object DryRunQueryJobStatistics {
    import DryQueryStatistics._

    val noEndTimesDryRunStatistics = DryRunQueryJobStatisticsApi(
      Int64Value(999999999L),
      dryQueryStatisticsWithReferenceAndSchema,
      Int64Value(1567513492669L),
      None,
      None
    )

  }

  object DryRunQueryJobResponse {
    import DryRunQueryJobStatistics._

    val selfLink = Some(
      "https://www.googleapis.com/bigquery/v2/projects/test-project/jobs/?location=us-1"
    )

    val successfulDryRunJobResponseApi = DryRunQueryJobResponseApi(
      JobStatuses.successfulJobStatus,
      DryRunJobReferenceApi(Some(location), bqProject),
      jobConfigurationApiQueryWriteTableDryRun,
      kind,
      etag,
      selfLink,
      noEndTimesDryRunStatistics
    )

  }

  object Fields {
    import com.permutive.google.bigquery.models.table.Field
    import com.permutive.google.bigquery.models.table.Field._

    val boolsWithDescription: Field =
      Field(
        Name("bools-with-desc"),
        SQLType.Boolean,
        Some(Mode.Repeated),
        Some("array of booleans"),
        None
      )

    val stringRequired: Field =
      Field(
        Name("string-required"),
        SQLType.String,
        Some(Mode.Required),
        None,
        None
      )

    val floatNullable: Field =
      Field(
        Name("float-nullable"),
        SQLType.Float,
        Some(Mode.Nullable),
        Some("nullable float"),
        None
      )

    val noMode: Field =
      Field(Name("no-mode"), SQLType.String, None, None, None)

    val nested: Field =
      Field(
        Name("outer-string"),
        SQLType.String,
        Some(Mode.Required),
        None,
        Some(
          NonEmptyList.of(
            Field(
              Name("inner-int"),
              SQLType.Integer,
              Some(Mode.Required),
              None,
              None
            )
          )
        )
      )

  }

  object JobQueryResult {
    val kind = "another-kind"
    val etag = "another-etag"

    val schema = SchemaApi(
      NonEmptyList
        .of(
          Fields.boolsWithDescription,
          Fields.stringRequired,
          Fields.floatNullable,
          Fields.nested,
          Fields.noMode
        )
    )

    val totalRows = UInt64Value(123456L)

    val pageToken = PageToken("next-page-token")

    val row1 = Json.obj(
      "col1" -> Json.fromString("foo"),
      "col2" -> Json.arr(Json.fromInt(1), Json.fromInt(2))
    )
    val row2 = Json.obj(
      "col1" -> Json.fromString("bar"),
      "col2" -> Json.arr(Json.fromInt(3), Json.fromInt(4))
    )

    val rows: List[Json] = List(row1, row2)

    val totalBytesProcessed = Int64Value(999L)

    val errors: List[ErrorProtoApi] =
      List(Errors.errorProtoApi1, Errors.errorProtoApi2)

    val numDmlAffectedRows = Int64Value(0L)

    val jobQueryResultCompleteAllFields = JobQueryResultApi(
      kind,
      etag,
      Some(schema),
      jobReferenceApi,
      Some(totalRows),
      Some(pageToken),
      Some(rows),
      Some(totalBytesProcessed),
      jobComplete = true,
      Some(errors),
      cacheHit = Some(true),
      Some(numDmlAffectedRows)
    )

    val jobQueryResultsCompleteMissingSchemaBytes =
      jobQueryResultCompleteAllFields.copy(
        schema = None,
        totalBytesProcessed = None
      )

    val jobQueryResultDmlComplete = JobQueryResultApi(
      kind,
      etag,
      Some(schema),
      jobReferenceApi,
      None,
      None,
      None,
      Some(totalBytesProcessed),
      jobComplete = true,
      None,
      cacheHit = Some(true),
      Some(numDmlAffectedRows)
    )

    val jobQueryResultIncompleteAllFields =
      jobQueryResultCompleteAllFields.copy(jobComplete = false)

    val jobQueryResultIncompleteWithErrors = JobQueryResultApi(
      kind,
      etag,
      None,
      jobReferenceApi,
      None,
      None,
      None,
      None,
      jobComplete = false,
      Some(errors),
      None,
      None
    )

    val jobQueryResultCompleteNoErrors =
      jobQueryResultCompleteAllFields.copy(errors = None)

    val jobQueryResultCompleteNoDml =
      jobQueryResultCompleteAllFields.copy(numDmlAffectedRows = None)

    val jobQueryResultCompleteNoDmlNoErrors =
      jobQueryResultCompleteNoDml.copy(errors = None)

    val jobQueryResultCompleteNoNextPageNoDml =
      jobQueryResultCompleteNoDml.copy(pageToken = None)

    val jobQueryResultCompleteNoRowsNoNextPageNoDml =
      jobQueryResultCompleteNoNextPageNoDml.copy(
        rows = None,
        totalRows = Some(UInt64Value(0L))
      )

    val jobQueryResultCompleteNoRowsNoNextPageNoDmlNoErrors =
      jobQueryResultCompleteNoRowsNoNextPageNoDml.copy(errors = None)

    val jobQueryResultCompleteRowsNoCount =
      jobQueryResultCompleteNoDml.copy(totalRows = None)
  }

  val queryWithParameters = JobConfigurationQueryBasicApi(
    query = Query("SELECT * FROM foo WHERE bar = @x"),
    useLegacySql = false,
    queryParameters = Some(
      NonEmptyList.of(
        QueryParameter(
          name = Some("x"),
          parameterType = QueryParameterType(
            `type` = SQLType.Struct,
            arrayType = None,
            structTypes = Some(
              List(
                StructType(
                  name = Some("baz"),
                  `type` = QueryParameterType(
                    `type` = SQLType.Int64,
                    arrayType = None,
                    structTypes = None
                  )
                ),
                StructType(
                  name = Some("qux"),
                  `type` = QueryParameterType(
                    `type` = SQLType.Array,
                    arrayType = Some(
                      QueryParameterType(
                        `type` = SQLType.String,
                        arrayType = None,
                        structTypes = None
                      )
                    ),
                    structTypes = None
                  )
                )
              )
            )
          ),
          parameterValue = QueryParameterValue(
            value = None,
            arrayValues = None,
            structValues = Some(
              ListMapLike(
                List(
                  "baz" -> QueryParameterValue(
                    value = Some("1"),
                    arrayValues = None,
                    structValues = None
                  ),
                  "qux" -> QueryParameterValue(
                    value = None,
                    arrayValues = Some(
                      List(
                        QueryParameterValue(
                          value = Some("hello"),
                          arrayValues = None,
                          structValues = None
                        )
                      )
                    ),
                    structValues = None
                  )
                )
              )
            )
          )
        )
      )
    )
  )
}
