package com.permutive.google.bigquery.rest.models.api.job

import com.permutive.google.bigquery.rest.models.TestData.JobQueryResult._
import com.permutive.testutils.{ResourceSupport, ResourceSupportMatchers}
import io.circe.literal._
import munit.FunSuite

class JobQueryResultApiSpec
    extends FunSuite
    with ResourceSupport
    with ResourceSupportMatchers {

  override val packageName = Some("google-bigquery")

  test("encode and decode between JSON properly with all fields") {
    checkEncodeDecode(
      "JobQueryResultApi-complete-all-fields.json",
      jobQueryResultCompleteAllFields
    )
  }

  test("encode and decode between JSON properly for an incomplete job") {
    checkEncodeDecode(
      "JobQueryResultApi-incomplete-with-errors.json",
      jobQueryResultIncompleteWithErrors
    )
  }

  test("encode and decode between JSON properly for a complete select job") {
    checkEncodeDecode(
      "JobQueryResultApi-complete-select-no-errors.json",
      jobQueryResultCompleteNoNextPageNoDml
    )
  }

  test(
    "encode and decode between JSON properly for a complete select with no rows"
  ) {
    checkEncodeDecode(
      "JobQueryResultApi-complete-select-no-errors-no-result-rows.json",
      jobQueryResultCompleteNoRowsNoNextPageNoDmlNoErrors
    )
  }

  test("decode a real response from BigQuery") {
    val testJson =
      json"""
        {
          "kind" : "bigquery#getQueryResultsResponse",
          "etag" : "0jD/ca2tOH4cKbKA3WtOQA==",
          "schema" : {
            "fields" : [
              {
                "name" : "last_run_time",
                "type" : "TIMESTAMP",
                "mode" : "NULLABLE"
              }
            ]
          },
          "jobReference" : {
            "projectId" : "permutive-bigquery-staging",
            "jobId" : "5d0f255a-2cbc-4381-a224-89f03bda0a22",
            "location" : "US"
          },
          "totalRows" : "1",
          "rows" : [
            {
              "f" : [
                {
                  "v" : "86400.0"
                }
              ]
            }
          ],
          "totalBytesProcessed" : "0",
          "jobComplete" : true,
          "cacheHit" : true
        }
      """
    assert(testJson.as[JobQueryResultApi].isRight)

    assert(
      readJsonResource("JobQueryResultApi-real-response.json")
        .as[JobQueryResultApi]
        .isRight
    )
  }

}
