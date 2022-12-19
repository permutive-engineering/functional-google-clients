package com.permutive.google.bigquery.rest.models.api.job

import com.permutive.google.bigquery.rest.models.TestData.JobQueryResult._
import com.permutive.testutils.{ResourceSupport, ResourceSupportMatchers}
import io.circe.parser._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JobQueryResultApiSpec
    extends AnyFlatSpec
    with Matchers
    with TypeCheckedTripleEquals
    with ResourceSupport
    with ResourceSupportMatchers {

  override val packageName = Some("google-bigquery")

  behavior.of("JobQueryResultApi")

  it should "encode and decode between JSON properly with all fields" in {
    checkEncodeDecode("JobQueryResultApi-complete-all-fields.json", jobQueryResultCompleteAllFields)
  }

  it should "encode and decode between JSON properly for an incomplete job" in {
    checkEncodeDecode("JobQueryResultApi-incomplete-with-errors.json", jobQueryResultIncompleteWithErrors)
  }

  it should "encode and decode between JSON properly for a complete select job" in {
    checkEncodeDecode("JobQueryResultApi-complete-select-no-errors.json", jobQueryResultCompleteNoNextPageNoDml)
  }

  it should "encode and decode between JSON properly for a complete select with no rows" in {
    checkEncodeDecode(
      "JobQueryResultApi-complete-select-no-errors-no-result-rows.json",
      jobQueryResultCompleteNoRowsNoNextPageNoDmlNoErrors,
    )
  }

  it should "decode a real response from BigQuery" in {
    val testJson =
      """
        |{
        |  "kind" : "bigquery#getQueryResultsResponse",
        |  "etag" : "0jD/ca2tOH4cKbKA3WtOQA==",
        |  "schema" : {
        |    "fields" : [
        |      {
        |        "name" : "last_run_time",
        |        "type" : "TIMESTAMP",
        |        "mode" : "NULLABLE"
        |      }
        |    ]
        |  },
        |  "jobReference" : {
        |    "projectId" : "permutive-bigquery-staging",
        |    "jobId" : "5d0f255a-2cbc-4381-a224-89f03bda0a22",
        |    "location" : "US"
        |  },
        |  "totalRows" : "1",
        |  "rows" : [
        |    {
        |      "f" : [
        |        {
        |          "v" : "86400.0"
        |        }
        |      ]
        |    }
        |  ],
        |  "totalBytesProcessed" : "0",
        |  "jobComplete" : true,
        |  "cacheHit" : true
        |}
      """.stripMargin

    parse(testJson).flatMap(_.as[JobQueryResultApi]) shouldBe Symbol("right")

    readJsonResource("JobQueryResultApi-real-response.json").as[JobQueryResultApi] shouldBe Symbol("right")
  }

}
