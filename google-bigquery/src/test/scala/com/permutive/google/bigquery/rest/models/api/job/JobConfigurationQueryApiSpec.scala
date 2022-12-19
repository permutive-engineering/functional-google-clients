package com.permutive.google.bigquery.rest.models.api.job

import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models.WriteDisposition
import com.permutive.google.bigquery.models.table.NewTypes.TableId
import com.permutive.google.bigquery.rest.models.TestData._
import com.permutive.google.bigquery.rest.models.api.TableReferenceApi
import com.permutive.testutils.{ResourceSupport, ResourceSupportMatchers}
import io.circe.Error
import io.circe.parser.parse
import io.circe.syntax._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor1}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JobConfigurationQueryApiSpec
    extends AnyFlatSpec
    with Matchers
    with TableDrivenPropertyChecks
    with TypeCheckedTripleEquals
    with ResourceSupport
    with ResourceSupportMatchers {

  override val packageName = Some("google-bigquery")

  behavior.of("JobConfigurationQueryApi.decode")

  private def decodeJobConfigurationQueryApi(s: String): JobConfigurationQueryApi = {
    val test: Either[Error, JobConfigurationQueryApi] =
      parse(s)
        .flatMap(_.as[JobConfigurationQueryApi])

    test shouldBe Symbol("right")
    val Right(result: JobConfigurationQueryApi) = test

    result
  }

  private[this] val tableReference = TableReferenceApi(
    BigQueryProjectName("foo"),
    DatasetId("bar"),
    TableId("baz"),
  )

  it should "decode a JobConfigurationQueryWriteTableApi where possible" in {
    val writeDisp = WriteDisposition.WriteTruncate
    val json =
      s"""{"query": "foo", "useLegacySql": false, "writeDisposition": "${writeDisp.entryName}", "destinationTable": ${tableReference.asJson}}"""

    val result  = decodeJobConfigurationQueryApi(json)
    val coerced = result.asInstanceOf[JobConfigurationQueryWriteTableApi]

    coerced.writeDisposition should ===(writeDisp)
    coerced.destinationTable should ===(tableReference)
  }

  val writeDisp = WriteDisposition.WriteTruncate

  val basicWriteJson: TableFor1[String] = Table(
    "json",
    s"""{"query": "foo", "useLegacySql": false, "INVALID": "${writeDisp.entryName}", "destinationTable": ${tableReference.asJson}}""",
    s"""{"query": "foo", "useLegacySql": false}""",
  )

  it should "fallback to decoding a JobConfigurationQueryBasicApi" in {
    forAll(basicWriteJson) { s =>
      val result  = decodeJobConfigurationQueryApi(s)
      val coerced = result.asInstanceOf[JobConfigurationQueryBasicApi]

      coerced.useLegacySql should ===(false)
    }
  }

  behavior.of("JobConfigurationQueryApi")

  val writeTableFilePath = "JobConfigurationQueryWriteTableApi.json"

  it should "encode and decode between JSON properly when it is a JobConfigurationQueryWriteTableApi" in {
    checkEncodeDecode(writeTableFilePath, jobQueryConfigWriteTableApi)
  }

  it should "encode and decode between JSON properly when it is generic but a JobConfigurationQueryWriteTableApi" in {
    val obj: JobConfigurationQueryApi = jobQueryConfigWriteTableApi

    checkEncodeDecode(writeTableFilePath, obj)
  }

  val basicFilePath = "JobConfigurationQueryBasicApi.json"

  it should "encode and decode between JSON properly when it is a JobConfigurationQueryBasicApi" in {
    checkEncodeDecode(basicFilePath, jobQueryConfigBasicApi)
  }

  it should "encode and decode between JSON properly when it is generic but a JobConfigurationQueryBasicApi" in {
    val obj: JobConfigurationQueryApi = jobQueryConfigBasicApi

    checkEncodeDecode(basicFilePath, obj)
  }

  it should "encode and decode between JSON properly when it is a query with parameters" in {
    val queryWithParametersPath = "QueryWithParameters.json"
    checkEncodeDecode(queryWithParametersPath, queryWithParameters)
  }

}
