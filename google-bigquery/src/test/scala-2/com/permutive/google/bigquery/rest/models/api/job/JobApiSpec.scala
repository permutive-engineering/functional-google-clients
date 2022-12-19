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

package com.permutive.google.bigquery.rest.models.api.job

import com.permutive.google.bigquery.rest.models.TestData.DryRunQueryJobResponse._
import com.permutive.google.bigquery.rest.models.TestData.JobStatuses._
import com.permutive.google.bigquery.rest.models.TestData.QueryJobResponses._
import com.permutive.google.bigquery.rest.models.TestData._
import com.permutive.testutils.{ResourceSupport, ResourceSupportMatchers}
import munit.FunSuite

class JobApiSpec
    extends FunSuite
    with ResourceSupport
    with ResourceSupportMatchers {

  override val packageName = Some("google-bigquery")

  test(
    "encode and decode between JSON properly when it includes a JobConfigurationApi.Query which writes to table"
  ) {
    checkEncodeDecode(
      "CreateQueryJobRequestApi-write-query.json",
      createQueryWriteTableJobRequestApi
    )
  }

  test(
    "encode and decode between JSON properly when it includes a JobConfigurationApi.Query which is basic (no dry run)"
  ) {
    checkEncodeDecode(
      "CreateQueryJobRequestApi-basic-query.json",
      createQueryBasicJobRequestApiNoDryRun
    )
  }

  test(
    "encode and decode between JSON properly when it includes a JobConfigurationApi.Query which is basic (with dry run)"
  ) {
    checkEncodeDecode(
      "CreateQueryJobRequestApi-basic-query-dry-run.json",
      createQueryBasicJobRequestApiDryRun
    )
  }

  // Stubbed out, functionality not used at the moment
  test(
    "encode and decode between JSON properly when it includes a JobConfigurationApi.Copy"
  ) {
    checkEncodeDecode(
      "CreateQueryJobRequestApi-copy.json",
      createCopyJobRequestApi
    )
  }

  test("decode a successful job properly") {
    checkDecode("JobStatusApi-successful.json", successfulJobStatus)
  }

  test("decode a failed job properly") {
    checkDecode("JobStatusApi-failed.json", failedJobStatus)
  }

  test("decode a successful job response") {
    checkDecode("QueryJobResponseApi-successful.json", successfulJobResponseApi)
  }

  test("decode a failed job response") {
    checkDecode("QueryJobResponseApi-failed.json", failedJobResponseApi)
  }

  test("decode a successful dry-run query job respoinse") {
    checkDecode(
      "DryRunQueryJobResponseApi-successful.json",
      successfulDryRunJobResponseApi
    )
  }

}
