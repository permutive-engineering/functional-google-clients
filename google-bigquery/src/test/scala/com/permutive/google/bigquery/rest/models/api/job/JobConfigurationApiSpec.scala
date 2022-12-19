package com.permutive.google.bigquery.rest.models.api.job

import com.permutive.google.bigquery.rest.models.TestData._
import com.permutive.testutils.{ResourceSupport, ResourceSupportMatchers}
import io.circe.Error
import io.circe.parser.parse
import io.circe.syntax._
import munit.FunSuite

class JobConfigurationApiSpec
    extends FunSuite
    with ResourceSupport
    with ResourceSupportMatchers {

  override val packageName = Some("google-bigquery")

  private def decodeJobConfigurationApi(s: String): JobConfigurationApi = {
    val test: Either[Error, JobConfigurationApi] =
      parse(s)
        .flatMap(_.as[JobConfigurationApi])

    assert(test.isRight)
    val Right(result: JobConfigurationApi) = test

    result
  }

  val validBasicQueries = List(
    s"""{"jobType": "Query", "query": ${jobQueryConfigBasicApi.asJson}}""",
    s"""{"jobType": "QUERY", "query": ${jobQueryConfigBasicApi.asJson}}""",
    s"""{"jobType": "QuERy", "query": ${jobQueryConfigBasicApi.asJson}}""",
    s"""{"jobType": "query", "query": ${jobQueryConfigBasicApi.asJson}}"""
  )

  test("successfully decode a basic Query regardless of case") {
    validBasicQueries.foreach { s =>
      val result = decodeJobConfigurationApi(s)

      assertEquals(
        result.asInstanceOf[JobConfigurationApi.Query],
        jobConfigurationQueryBasicNoDryRun
      )
    }
  }

  val validWroteTableQueries = List(
    s"""{"jobType": "Query", "query": ${jobQueryConfigWriteTableApi.asJson}}""",
    s"""{"jobType": "QUERY", "query": ${jobQueryConfigWriteTableApi.asJson}}""",
    s"""{"jobType": "QuERy", "query": ${jobQueryConfigWriteTableApi.asJson}}""",
    s"""{"jobType": "query", "query": ${jobQueryConfigWriteTableApi.asJson}}"""
  )

  test("successfully decode a write table Query regardless of case") {
    validWroteTableQueries.foreach { s =>
      val result = decodeJobConfigurationApi(s)

      assertEquals(
        result.asInstanceOf[JobConfigurationApi.Query],
        jobConfigurationApiQueryWriteTable
      )
    }
  }

  test("encode and decode a basic Query between JSON (no dry run)") {
    checkEncodeDecode(
      "JobConfigurationApi#Query-basic.Json",
      jobConfigurationQueryBasicNoDryRun: JobConfigurationApi
    )
  }

  test("encode and decode a basic Query between JSON (dry run)") {
    checkEncodeDecode(
      "JobConfigurationApi#Query-basic-dry-run.Json",
      jobConfigurationQueryBasicDryRun: JobConfigurationApi
    )
  }

  test("encode and decode a write table Query between JSON") {
    checkEncodeDecode(
      "JobConfigurationApi#Query-write-table.Json",
      jobConfigurationApiQueryWriteTable: JobConfigurationApi
    )
  }

  val validCopies = List(
    """{"jobType": "Copy"}""",
    """{"jobType": "COPY"}""",
    """{"jobType": "CoPY"}""",
    """{"jobType": "copy"}"""
  )

  test("successfully decode a Copy regardless of case") {
    validCopies.foreach { s =>
      val result = decodeJobConfigurationApi(s)

      assertEquals(
        result.asInstanceOf[JobConfigurationApi.Copy.type],
        JobConfigurationApi.Copy
      )
    }
  }

  test("encode and decode a Copy between JSON") {
    checkEncodeDecode(
      "JobConfigurationApi#Copy.Json",
      JobConfigurationApi.Copy: JobConfigurationApi
    )
  }

  val validExtracts = List(
    """{"jobType": "Extract"}""",
    """{"jobType": "EXTRACT"}""",
    """{"jobType": "extract"}"""
  )

  test("successfully decode an Extract regardless of case") {
    validExtracts.foreach { s =>
      val result = decodeJobConfigurationApi(s)

      assertEquals(
        result.asInstanceOf[JobConfigurationApi.Extract.type],
        JobConfigurationApi.Extract
      )
    }
  }

  test("encode and decode an Extract between JSON") {
    checkEncodeDecode(
      "JobConfigurationApi#Extract.Json",
      JobConfigurationApi.Extract: JobConfigurationApi
    )
  }

  val validLoads = List(
    """{"jobType": "Load"}""",
    """{"jobType": "LOAD"}""",
    """{"jobType": "load"}"""
  )

  test("successfully decode a Load regardless of case") {
    validLoads.foreach { s =>
      val result = decodeJobConfigurationApi(s)

      assertEquals(
        result.asInstanceOf[JobConfigurationApi.Load.type],
        JobConfigurationApi.Load
      )
    }
  }

  test("encode and decode a Load between JSON") {
    checkEncodeDecode(
      "JobConfigurationApi#Load.Json",
      JobConfigurationApi.Load: JobConfigurationApi
    )
  }

  val validUnknowns = List(
    """{"jobType": "Unknown"}""",
    """{"jobType": "UNKNOWN"}""",
    """{"jobType": "unknown"}"""
  )

  test("successfully decode an Unknown regardless of case") {
    validUnknowns.foreach { s =>
      val result = decodeJobConfigurationApi(s)

      assertEquals(
        result.asInstanceOf[JobConfigurationApi.Unknown.type],
        JobConfigurationApi.Unknown
      )
    }
  }

  test("encode and decode an Unknown between JSON") {
    checkEncodeDecode(
      "JobConfigurationApi#Unknown.Json",
      JobConfigurationApi.Unknown: JobConfigurationApi
    )
  }

  val invalidJobTypes = List(
    """{"jobType": "cy"}""",
    """{}""",
    """{"jobType": "extr"}""",
    """{"jobType": "unkn"}""",
    """{"jobType": "q"}"""
  )

  test("reject invalid job types") {
    invalidJobTypes.foreach { s =>
      val test: Either[Error, JobConfigurationApi] =
        parse(s)
          .flatMap(_.as[JobConfigurationApi])

      assert(test.isLeft)
    }
  }

}
