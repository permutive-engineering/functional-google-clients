package com.permutive.google.bigquery.rest.models.job.results

import cats.data.NonEmptyList
import com.permutive.google.bigquery.rest.models.TestData.Errors._
import com.permutive.google.bigquery.rest.models.TestData.JobQueryResult._
import com.permutive.google.bigquery.rest.models.api.job.JobQueryResultApi
import com.permutive.google.bigquery.rest.models.job.JobError
import com.permutive.google.bigquery.rest.models.job.results.NewTypes.JobResultRow
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.Assertion
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class QueryJobResultsSpec extends AnyFlatSpec with Matchers with TypeCheckedTripleEquals {

  behavior.of("QueryJobResults.fromResponse")

  private def testIncompleteDmlJob(testData: JobQueryResultApi): Assertion = {
    val either = QueryJobResults.fromResponse(testData)

    either shouldBe Symbol("right")
    val Right(res: IncompleteJob) = either

    res.errors shouldBe defined
    res.errors.get should ===(JobError.many(testData.errors.get).get)
  }

  it should "return an IncompleteJob " in {
    testIncompleteDmlJob(jobQueryResultIncompleteWithErrors)
  }

  it should "return an IncompleteJob even if there are rows present, if the indicator says incomplete" in {
    testIncompleteDmlJob(jobQueryResultIncompleteAllFields)
  }

  private def testCompleteDmlJob(testData: JobQueryResultApi): Assertion = {
    val either = QueryJobResults.fromResponse(testData)

    either shouldBe Symbol("right")
    val Right(res: CompleteDmlJob) = either

    res.schema should ===(testData.schema.get.fields)
    res.totalBytesProcessed should ===(testData.totalBytesProcessed.get.value)
    res.cacheHit should ===(testData.cacheHit.get)
    res.affectedRows should ===(testData.numDmlAffectedRows.get.value)
  }

  it should "return a CompleteDmlJob if the row count is present" in {
    testCompleteDmlJob(jobQueryResultCompleteAllFields)
  }

  it should "return a CompleteDmlJob if the row count is present, even if there are rows present" in {
    val testData = jobQueryResultCompleteAllFields
    testData.rows shouldBe defined
    testData.totalRows shouldBe defined

    testCompleteDmlJob(testData)
  }

  private def testCompleteSelectJobNoErrorsNoRows(testData: JobQueryResultApi): CompleteSelectJob = {
    val either = QueryJobResults.fromResponse(testData)

    either shouldBe Symbol("right")
    val Right(res: CompleteSelectJob) = either

    res.schema should ===(testData.schema.get.fields)
    res.location should ===(testData.jobReference.location)
    res.totalRows should ===(testData.totalRows.get.value)
    res.nextPageToken should ===(testData.pageToken)
    res.totalBytesProcessed should ===(testData.totalBytesProcessed.get.value)
    res.cacheHit should ===(testData.cacheHit.get)

    res
  }

  private def testRows(testData: JobQueryResultApi, res: CompleteSelectJob): Assertion =
    res.rows should ===(NonEmptyList.fromList(testData.rows.get.map(JobResultRow.apply)))

  private def testNoRows(res: CompleteSelectJob): Assertion =
    res.rows should ===(None)

  private def testErrors(testData: JobQueryResultApi, res: CompleteSelectJob): Assertion =
    res.errors.get should ===(JobError.many(testData.errors.get).get)

  private def testNoErrors(res: CompleteSelectJob): Assertion =
    res.errors should ===(None)

  private def testCompleteSelectJobWithErrorsWithRows(testData: JobQueryResultApi): Assertion = {
    val res = testCompleteSelectJobNoErrorsNoRows(testData)
    testRows(testData, res)
    testErrors(testData, res)
  }

  private def testCompleteSelectJobWithErrorsNoRows(testData: JobQueryResultApi): Assertion = {
    val res = testCompleteSelectJobNoErrorsNoRows(testData)
    testErrors(testData, res)
    testNoRows(res)
  }

  private def testCompleteSelectJobNoErrorsWithRows(testData: JobQueryResultApi): Assertion = {
    val res = testCompleteSelectJobNoErrorsNoRows(testData)
    testRows(testData, res)
    testNoErrors(res)
  }

  it should "return a CompleteSelectJob when no DML affected count is present and rows are" in {
    testCompleteSelectJobWithErrorsWithRows(jobQueryResultCompleteNoDml)
    testCompleteSelectJobWithErrorsWithRows(jobQueryResultCompleteNoNextPageNoDml)
    testCompleteSelectJobNoErrorsWithRows(jobQueryResultCompleteNoDmlNoErrors)
  }

  it should "return a CompleteSelectJob when no DML affected count is present and no rows are" in {
    testCompleteSelectJobWithErrorsNoRows(jobQueryResultCompleteNoRowsNoNextPageNoDml)
    testCompleteSelectJobNoErrorsNoRows(jobQueryResultCompleteNoRowsNoNextPageNoDmlNoErrors)
  }

  private def testErrors(testData: JobQueryResultApi): QueryJobResults = {
    val either = QueryJobResults.fromResponse(testData)

    either shouldBe Symbol("right")
    val Right(res) = either

    res.errors shouldBe defined

    val firstError = res.errors.get.head

    firstError.message should ===(errorProtoApi1.message)
    firstError.location should ===(errorProtoApi1.location)
    firstError.reason should ===(errorProtoApi1.reason)

    res
  }

  it should "convert errors into JobError properly when they are present" in {
    val expectedDml = testErrors(jobQueryResultCompleteAllFields)
    expectedDml.asInstanceOf[CompleteDmlJob]

    val expectedIncomplete = testErrors(jobQueryResultIncompleteWithErrors)
    expectedIncomplete.asInstanceOf[IncompleteJob]

    val expectedSelect = testErrors(jobQueryResultCompleteNoDml)
    expectedSelect.asInstanceOf[CompleteSelectJob]
  }

  it should "convert no errors properly" in {
    val either = QueryJobResults.fromResponse(jobQueryResultCompleteNoErrors)

    either shouldBe Symbol("right")
    val Right(res) = either

    res.errors shouldBe Symbol("empty")
  }

  private def testMissingFields(
    testData: JobQueryResultApi,
    description: String,
    expectedMissing: Set[String],
  ): Assertion = {
    val either = QueryJobResults.fromResponse(testData)

    either shouldBe Symbol("left")
    val Left(res) = either

    res.description should include(description)
    res.missingFields.toList.toSet should ===(expectedMissing)
    res.missingFields.length should ===(expectedMissing.size)
  }

  it should "collect missing fields reporting all the failures" in {
    testMissingFields(jobQueryResultCompleteRowsNoCount, "CompleteSelectJob", Set("totalRows"))
    testMissingFields(jobQueryResultsCompleteMissingSchemaBytes, "CompleteDmlJob", Set("schema", "totalBytesProcessed"))
  }

}
