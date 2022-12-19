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

package com.permutive.google.bigquery.rest.models.job

import cats.data.NonEmptyList
import com.permutive.google.bigquery.rest.models.TestData.Errors._
import com.permutive.google.bigquery.rest.models.TestData.JobStatuses._
import com.permutive.google.bigquery.rest.models.TestData.QueryJobResponses._
import com.permutive.google.bigquery.rest.models.TestData._
import munit.FunSuite

class JobSpec extends FunSuite {

  test("correctly handle a complete, successful response to a SuccessfulJob") {
    val res =
      Job.fromResponse(successfulJobResponseApi).asInstanceOf[SuccessfulJob]

    assertEquals(res.id, jobId)
  }

  test("correctly handle a complete, failed response to a FailedJob") {
    val res = Job.fromResponse(failedJobResponseApi).asInstanceOf[FailedJob]

    assertEquals(res.id, jobId)
    assertEquals(res.jobError, JobError.one(errorProtoApi1))
    assertEquals(
      res.jobErrors,
      NonEmptyList.of(errorProtoApi2, errorProtoApi3).map(JobError.one)
    )
  }

  test("correctly handle a running response to an IncompleteJob") {
    val res =
      Job.fromResponse(runningJobResponseApi).asInstanceOf[IncompleteJob]

    assertEquals(res.id, jobId)
    assertEquals(res.state, JobState.Running)
  }

  test("correctly handle a pending response to an IncompleteJob") {
    val res =
      Job.fromResponse(pendingJobResponseApi).asInstanceOf[IncompleteJob]

    assertEquals(res.id, jobId)
    assertEquals(res.state, JobState.Pending)
  }

  test(
    "correctly return a SuccessfulJob even if there are errors in the errors list"
  ) {
    val responseApi =
      successfulJobResponseApi.copy(status = completedJobStatusListNoError)

    val res = Job.fromResponse(responseApi).asInstanceOf[SuccessfulJob]

    assertEquals(res.id, jobId)
  }

  test(
    "correctly return a FailedJob even if there are no errors in the errors list, but there are in errorResponse"
  ) {
    val responseApi =
      successfulJobResponseApi.copy(status = failedJobStatusNoList)

    val res = Job.fromResponse(responseApi).asInstanceOf[FailedJob]

    assertEquals(res.id, jobId)
    assertEquals(res.jobError, JobError.one(errorProtoApi1))
    assertEquals(res.jobErrors, NonEmptyList.one(JobError.one(errorProtoApi1)))
  }

}
