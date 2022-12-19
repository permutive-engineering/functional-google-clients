package com.permutive.google.bigquery.rest.models.job

import cats.data.NonEmptyList
import com.permutive.google.bigquery.rest.models.TestData.Errors._
import com.permutive.google.bigquery.rest.models.TestData.JobStatuses._
import com.permutive.google.bigquery.rest.models.TestData.QueryJobResponses._
import com.permutive.google.bigquery.rest.models.TestData._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JobSpec extends AnyFlatSpec with Matchers with TypeCheckedTripleEquals {

  behavior.of("Job.fromResponse")

  it should "correctly handle a complete, successful response to a SuccessfulJob" in {
    val res = Job.fromResponse(successfulJobResponseApi).asInstanceOf[SuccessfulJob]

    res.id should ===(jobId)
  }

  it should "correctly handle a complete, failed response to a FailedJob" in {
    val res = Job.fromResponse(failedJobResponseApi).asInstanceOf[FailedJob]

    res.id should ===(jobId)
    res.jobError should ===(JobError.one(errorProtoApi1))
    res.jobErrors should ===(NonEmptyList.of(errorProtoApi2, errorProtoApi3).map(JobError.one))
  }

  it should "correctly handle a running response to an IncompleteJob" in {
    val res = Job.fromResponse(runningJobResponseApi).asInstanceOf[IncompleteJob]

    res.id should ===(jobId)
    res.state should ===(JobState.Running)
  }

  it should "correctly handle a pending response to an IncompleteJob" in {
    val res = Job.fromResponse(pendingJobResponseApi).asInstanceOf[IncompleteJob]

    res.id should ===(jobId)
    res.state should ===(JobState.Pending)
  }

  it should "correctly return a SuccessfulJob even if there are errors in the errors list" in {
    val responseApi = successfulJobResponseApi.copy(status = completedJobStatusListNoError)

    val res = Job.fromResponse(responseApi).asInstanceOf[SuccessfulJob]

    res.id should ===(jobId)
  }

  it should "correctly return a FailedJob even if there are no errors in the errors list, but there are in errorResponse" in {
    val responseApi = successfulJobResponseApi.copy(status = failedJobStatusNoList)

    val res = Job.fromResponse(responseApi).asInstanceOf[FailedJob]

    res.id should ===(jobId)
    res.jobError should ===(JobError.one(errorProtoApi1))
    res.jobErrors should ===(NonEmptyList.one(JobError.one(errorProtoApi1)))
  }

}
