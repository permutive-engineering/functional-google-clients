package com.permutive.google.bigquery.rest.models.api.job

import com.permutive.google.bigquery.rest.models.TestData._
import com.permutive.testutils.{ResourceSupport, ResourceSupportMatchers}
import io.circe.Error
import io.circe.parser.parse
import io.circe.syntax._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor1}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JobConfigurationApiSpec
    extends AnyFlatSpec
    with Matchers
    with TableDrivenPropertyChecks
    with TypeCheckedTripleEquals
    with ResourceSupport
    with ResourceSupportMatchers {

  override val packageName = Some("google-bigquery")

  behavior.of("JobConfigurationApi.decode")

  private def decodeJobConfigurationApi(s: String): JobConfigurationApi = {
    val test: Either[Error, JobConfigurationApi] =
      parse(s)
        .flatMap(_.as[JobConfigurationApi])

    test shouldBe Symbol("right")
    val Right(result: JobConfigurationApi) = test

    result
  }

  val validBasicQueries: TableFor1[String] = Table(
    "json",
    s"""{"jobType": "Query", "query": ${jobQueryConfigBasicApi.asJson}}""",
    s"""{"jobType": "QUERY", "query": ${jobQueryConfigBasicApi.asJson}}""",
    s"""{"jobType": "QuERy", "query": ${jobQueryConfigBasicApi.asJson}}""",
    s"""{"jobType": "query", "query": ${jobQueryConfigBasicApi.asJson}}""",
  )

  it should "successfully decode a basic Query regardless of case" in {
    forEvery(validBasicQueries) { s =>
      val result = decodeJobConfigurationApi(s)

      result.asInstanceOf[JobConfigurationApi.Query] should ===(jobConfigurationQueryBasicNoDryRun)
    }
  }

  val validWroteTableQueries: TableFor1[String] = Table(
    "json",
    s"""{"jobType": "Query", "query": ${jobQueryConfigWriteTableApi.asJson}}""",
    s"""{"jobType": "QUERY", "query": ${jobQueryConfigWriteTableApi.asJson}}""",
    s"""{"jobType": "QuERy", "query": ${jobQueryConfigWriteTableApi.asJson}}""",
    s"""{"jobType": "query", "query": ${jobQueryConfigWriteTableApi.asJson}}""",
  )

  it should "successfully decode a write table Query regardless of case" in {
    forEvery(validWroteTableQueries) { s =>
      val result = decodeJobConfigurationApi(s)

      result.asInstanceOf[JobConfigurationApi.Query] should ===(jobConfigurationApiQueryWriteTable)
    }
  }

  it should "encode and decode a basic Query between JSON (no dry run)" in {
    checkEncodeDecode("JobConfigurationApi#Query-basic.Json", jobConfigurationQueryBasicNoDryRun: JobConfigurationApi)
  }

  it should "encode and decode a basic Query between JSON (dry run)" in {
    checkEncodeDecode(
      "JobConfigurationApi#Query-basic-dry-run.Json",
      jobConfigurationQueryBasicDryRun: JobConfigurationApi,
    )
  }

  it should "encode and decode a write table Query between JSON" in {
    checkEncodeDecode(
      "JobConfigurationApi#Query-write-table.Json",
      jobConfigurationApiQueryWriteTable: JobConfigurationApi,
    )
  }

  val validCopies: TableFor1[String] = Table(
    "json",
    """{"jobType": "Copy"}""",
    """{"jobType": "COPY"}""",
    """{"jobType": "CoPY"}""",
    """{"jobType": "copy"}""",
  )

  it should "successfully decode a Copy regardless of case" in {
    forEvery(validCopies) { s =>
      val result = decodeJobConfigurationApi(s)

      result.asInstanceOf[JobConfigurationApi.Copy.type] should ===(JobConfigurationApi.Copy)
    }
  }

  it should "encode and decode a Copy between JSON" in {
    checkEncodeDecode("JobConfigurationApi#Copy.Json", JobConfigurationApi.Copy: JobConfigurationApi)
  }

  val validExtracts: TableFor1[String] = Table(
    "json",
    """{"jobType": "Extract"}""",
    """{"jobType": "EXTRACT"}""",
    """{"jobType": "extract"}""",
  )

  it should "successfully decode an Extract regardless of case" in {
    forEvery(validExtracts) { s =>
      val result = decodeJobConfigurationApi(s)

      result.asInstanceOf[JobConfigurationApi.Extract.type] should ===(JobConfigurationApi.Extract)
    }
  }

  it should "encode and decode an Extract between JSON" in {
    checkEncodeDecode("JobConfigurationApi#Extract.Json", JobConfigurationApi.Extract: JobConfigurationApi)
  }

  val validLoads: TableFor1[String] = Table(
    "json",
    """{"jobType": "Load"}""",
    """{"jobType": "LOAD"}""",
    """{"jobType": "load"}""",
  )

  it should "successfully decode a Load regardless of case" in {
    forEvery(validLoads) { s =>
      val result = decodeJobConfigurationApi(s)

      result.asInstanceOf[JobConfigurationApi.Load.type] should ===(JobConfigurationApi.Load)
    }
  }

  it should "encode and decode a Load between JSON" in {
    checkEncodeDecode("JobConfigurationApi#Load.Json", JobConfigurationApi.Load: JobConfigurationApi)
  }

  val validUnknowns: TableFor1[String] = Table(
    "json",
    """{"jobType": "Unknown"}""",
    """{"jobType": "UNKNOWN"}""",
    """{"jobType": "unknown"}""",
  )

  it should "successfully decode an Unknown regardless of case" in {
    forEvery(validUnknowns) { s =>
      val result = decodeJobConfigurationApi(s)

      result.asInstanceOf[JobConfigurationApi.Unknown.type] should ===(JobConfigurationApi.Unknown)
    }
  }

  it should "encode and decode an Unknown between JSON" in {
    checkEncodeDecode("JobConfigurationApi#Unknown.Json", JobConfigurationApi.Unknown: JobConfigurationApi)
  }

  val invalidJobTypes: TableFor1[String] = Table(
    "json",
    """{"jobType": "cy"}""",
    """{}""",
    """{"jobType": "extr"}""",
    """{"jobType": "unkn"}""",
    """{"jobType": "q"}""",
  )

  it should "reject invalid job types" in {
    forAll(invalidJobTypes) { s =>
      val test: Either[Error, JobConfigurationApi] =
        parse(s)
          .flatMap(_.as[JobConfigurationApi])

      test shouldBe Symbol("left")
    }
  }

}
