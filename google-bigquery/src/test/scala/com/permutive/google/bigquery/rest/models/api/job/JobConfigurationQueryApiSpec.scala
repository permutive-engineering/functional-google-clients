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
import munit.FunSuite

class JobConfigurationQueryApiSpec
    extends FunSuite
    with ResourceSupport
    with ResourceSupportMatchers {

  override val packageName = Some("google-bigquery")

  private def decodeJobConfigurationQueryApi(
      s: String
  ): JobConfigurationQueryApi = {
    val test: Either[Error, JobConfigurationQueryApi] =
      parse(s)
        .flatMap(_.as[JobConfigurationQueryApi])

    assert(test.isRight)
    val Right(result: JobConfigurationQueryApi) = test

    result
  }

  private[this] val tableReference = TableReferenceApi(
    BigQueryProjectName("foo"),
    DatasetId("bar"),
    TableId("baz")
  )

  test("decode a JobConfigurationQueryWriteTableApi where possible") {
    val writeDisp = WriteDisposition.WriteTruncate
    val json =
      s"""{"query": "foo", "useLegacySql": false, "writeDisposition": "${writeDisp.entryName}", "destinationTable": ${tableReference.asJson}}"""

    val result = decodeJobConfigurationQueryApi(json)
    val coerced = result.asInstanceOf[JobConfigurationQueryWriteTableApi]

    assertEquals(coerced.writeDisposition, writeDisp)
    assertEquals(coerced.destinationTable, tableReference)
  }

  val writeDisp = WriteDisposition.WriteTruncate

  val basicWriteJson = List(
    s"""{"query": "foo", "useLegacySql": false, "INVALID": "${writeDisp.entryName}", "destinationTable": ${tableReference.asJson}}""",
    s"""{"query": "foo", "useLegacySql": false}"""
  )

  test("fallback to decoding a JobConfigurationQueryBasicApi") {
    basicWriteJson.foreach { s =>
      val result = decodeJobConfigurationQueryApi(s)
      val coerced = result.asInstanceOf[JobConfigurationQueryBasicApi]

      assertEquals(coerced.useLegacySql, false)
    }
  }

  val writeTableFilePath = "JobConfigurationQueryWriteTableApi.json"

  test(
    "encode and decode between JSON properly when it is a JobConfigurationQueryWriteTableApi"
  ) {
    checkEncodeDecode(writeTableFilePath, jobQueryConfigWriteTableApi)
  }

  test(
    "encode and decode between JSON properly when it is generic but a JobConfigurationQueryWriteTableApi"
  ) {
    val obj: JobConfigurationQueryApi = jobQueryConfigWriteTableApi

    checkEncodeDecode(writeTableFilePath, obj)
  }

  val basicFilePath = "JobConfigurationQueryBasicApi.json"

  test(
    "encode and decode between JSON properly when it is a JobConfigurationQueryBasicApi"
  ) {
    checkEncodeDecode(basicFilePath, jobQueryConfigBasicApi)
  }

  test(
    "encode and decode between JSON properly when it is generic but a JobConfigurationQueryBasicApi"
  ) {
    val obj: JobConfigurationQueryApi = jobQueryConfigBasicApi

    checkEncodeDecode(basicFilePath, obj)
  }

  test(
    "encode and decode between JSON properly when it is a query with parameters"
  ) {
    val queryWithParametersPath = "QueryWithParameters.json"
    checkEncodeDecode(queryWithParametersPath, queryWithParameters)
  }

}
