package com.permutive.google.bigquery.rest.models.job.results

import cats.data.NonEmptyList
import com.permutive.google.bigquery.rest.models.TestData.Errors._
import com.permutive.google.bigquery.rest.models.TestData.JobQueryResult._
import com.permutive.google.bigquery.rest.models.api.job.JobQueryResultApi
import com.permutive.google.bigquery.rest.models.job.JobError
import com.permutive.google.bigquery.rest.models.job.results.NewTypes.JobResultRow
import munit.FunSuite

class QueryJobResultsSpec extends FunSuite {

  private def testIncompleteDmlJob(testData: JobQueryResultApi) = {
    val either = QueryJobResults.fromResponse(testData)

    assert(either.isRight)
    val Right(res: IncompleteJob) = either

    assert(res.errors.isDefined)
    assertEquals(res.errors, JobError.many(testData.errors.get))
  }

  test("return an IncompleteJob ") {
    testIncompleteDmlJob(jobQueryResultIncompleteWithErrors)
  }

  test(
    "return an IncompleteJob even if there are rows present, if the indicator says incomplete"
  ) {
    testIncompleteDmlJob(jobQueryResultIncompleteAllFields)
  }

  private def testCompleteDmlJob(testData: JobQueryResultApi) = {
    val either = QueryJobResults.fromResponse(testData)

    assert(either.isRight)
    val Right(res: CompleteDmlJob) = either

    assertEquals(res.schema, testData.schema.get.fields)
    assertEquals(
      res.totalBytesProcessed,
      testData.totalBytesProcessed.get.value
    )
    assertEquals(res.cacheHit, testData.cacheHit.get)
    assertEquals(res.affectedRows, testData.numDmlAffectedRows.get.value)
  }

  test("return a CompleteDmlJob if the row count is present") {
    testCompleteDmlJob(jobQueryResultCompleteAllFields)
  }

  test(
    "return a CompleteDmlJob if the row count is present, even if there are rows present"
  ) {
    val testData = jobQueryResultCompleteAllFields
    assert(testData.rows.isDefined)
    assert(testData.totalRows.isDefined)

    testCompleteDmlJob(testData)
  }

  private def testCompleteSelectJobNoErrorsNoRows(
      testData: JobQueryResultApi
  ): CompleteSelectJob = {
    val either = QueryJobResults.fromResponse(testData)

    assert(either.isRight)
    val Right(res: CompleteSelectJob) = either

    assertEquals(res.schema, testData.schema.get.fields)
    assertEquals(res.location, testData.jobReference.location)
    assertEquals(res.totalRows, testData.totalRows.get.value)
    assertEquals(res.nextPageToken, testData.pageToken)
    assertEquals(
      res.totalBytesProcessed,
      testData.totalBytesProcessed.get.value
    )
    assertEquals(res.cacheHit, testData.cacheHit.get)

    res
  }

  private def testRows(testData: JobQueryResultApi, res: CompleteSelectJob) =
    assertEquals(
      res.rows,
      NonEmptyList.fromList(testData.rows.get.map(JobResultRow.apply))
    )

  private def testNoRows(res: CompleteSelectJob) =
    assertEquals(res.rows, None)

  private def testErrors(testData: JobQueryResultApi, res: CompleteSelectJob) =
    assertEquals(res.errors, JobError.many(testData.errors.get))

  private def testNoErrors(res: CompleteSelectJob) =
    assertEquals(res.errors, None)

  private def testCompleteSelectJobWithErrorsWithRows(
      testData: JobQueryResultApi
  ) = {
    val res = testCompleteSelectJobNoErrorsNoRows(testData)
    testRows(testData, res)
    testErrors(testData, res)
  }

  private def testCompleteSelectJobWithErrorsNoRows(
      testData: JobQueryResultApi
  ) = {
    val res = testCompleteSelectJobNoErrorsNoRows(testData)
    testErrors(testData, res)
    testNoRows(res)
  }

  private def testCompleteSelectJobNoErrorsWithRows(
      testData: JobQueryResultApi
  ) = {
    val res = testCompleteSelectJobNoErrorsNoRows(testData)
    testRows(testData, res)
    testNoErrors(res)
  }

  test(
    "return a CompleteSelectJob when no DML affected count is present and rows are"
  ) {
    testCompleteSelectJobWithErrorsWithRows(jobQueryResultCompleteNoDml)
    testCompleteSelectJobWithErrorsWithRows(
      jobQueryResultCompleteNoNextPageNoDml
    )
    testCompleteSelectJobNoErrorsWithRows(jobQueryResultCompleteNoDmlNoErrors)
  }

  test(
    "return a CompleteSelectJob when no DML affected count is present and no rows are"
  ) {
    testCompleteSelectJobWithErrorsNoRows(
      jobQueryResultCompleteNoRowsNoNextPageNoDml
    )
    testCompleteSelectJobNoErrorsNoRows(
      jobQueryResultCompleteNoRowsNoNextPageNoDmlNoErrors
    )
  }

  private def testErrors(testData: JobQueryResultApi): QueryJobResults = {
    val either = QueryJobResults.fromResponse(testData)

    assert(either.isRight)
    val Right(res) = either

    assert(res.errors.isDefined)

    val firstError = res.errors.get.head

    assertEquals(firstError.message, errorProtoApi1.message)
    assertEquals(firstError.location, errorProtoApi1.location)
    assertEquals(firstError.reason, errorProtoApi1.reason)

    res
  }

  test("convert errors into JobError properly when they are present") {
    val expectedDml = testErrors(jobQueryResultCompleteAllFields)
    expectedDml.asInstanceOf[CompleteDmlJob]

    val expectedIncomplete = testErrors(jobQueryResultIncompleteWithErrors)
    expectedIncomplete.asInstanceOf[IncompleteJob]

    val expectedSelect = testErrors(jobQueryResultCompleteNoDml)
    expectedSelect.asInstanceOf[CompleteSelectJob]
  }

  test("convert no errors properly") {
    val either = QueryJobResults.fromResponse(jobQueryResultCompleteNoErrors)

    assert(either.isRight)
    val Right(res) = either

    assert(res.errors.isEmpty)
  }

  private def testMissingFields(
      testData: JobQueryResultApi,
      description: String,
      expectedMissing: Set[String]
  ) = {
    val either = QueryJobResults.fromResponse(testData)

    assert(either.isLeft)
    val Left(res) = either

    assert(res.description.contains(description))
    assertEquals(res.missingFields.toList.toSet, expectedMissing)
    assertEquals(res.missingFields.length, expectedMissing.size)
  }

  test("collect missing fields reporting all the failures") {
    testMissingFields(
      jobQueryResultCompleteRowsNoCount,
      "CompleteSelectJob",
      Set("totalRows")
    )
    testMissingFields(
      jobQueryResultsCompleteMissingSchemaBytes,
      "CompleteDmlJob",
      Set("schema", "totalBytesProcessed")
    )
  }

}
