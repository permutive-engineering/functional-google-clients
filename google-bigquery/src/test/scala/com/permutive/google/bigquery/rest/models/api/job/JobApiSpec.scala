package com.permutive.google.bigquery.rest.models.api.job

import com.permutive.google.bigquery.rest.models.TestData.JobStatuses._
import com.permutive.google.bigquery.rest.models.TestData.QueryJobResponses._
import com.permutive.google.bigquery.rest.models.TestData.DryRunQueryJobResponse._
import com.permutive.google.bigquery.rest.models.TestData._
import com.permutive.testutils.{ResourceSupport, ResourceSupportMatchers}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JobApiSpec
    extends AnyFlatSpec
    with Matchers
    with TypeCheckedTripleEquals
    with ResourceSupport
    with ResourceSupportMatchers {

  override val packageName = Some("google-bigquery")

  behavior.of("CreateQueryJobRequestApi")

  it should "encode and decode between JSON properly when it includes a JobConfigurationApi.Query which writes to table" in {
    checkEncodeDecode("CreateQueryJobRequestApi-write-query.json", createQueryWriteTableJobRequestApi)
  }

  it should "encode and decode between JSON properly when it includes a JobConfigurationApi.Query which is basic (no dry run)" in {
    checkEncodeDecode("CreateQueryJobRequestApi-basic-query.json", createQueryBasicJobRequestApiNoDryRun)
  }

  it should "encode and decode between JSON properly when it includes a JobConfigurationApi.Query which is basic (with dry run)" in {
    checkEncodeDecode("CreateQueryJobRequestApi-basic-query-dry-run.json", createQueryBasicJobRequestApiDryRun)
  }

  // Stubbed out, functionality not used at the moment
  it should "encode and decode between JSON properly when it includes a JobConfigurationApi.Copy" in {
    checkEncodeDecode("CreateQueryJobRequestApi-copy.json", createCopyJobRequestApi)
  }

  behavior.of("JobStatusApi")

  it should "decode a successful job properly" in {
    checkDecode("JobStatusApi-successful.json", successfulJobStatus)
  }

  it should "decode a failed job properly" in {
    checkDecode("JobStatusApi-failed.json", failedJobStatus)
  }

  behavior.of("QueryJobResponseApi")

  it should "decode a successful job response" in {
    checkDecode("QueryJobResponseApi-successful.json", successfulJobResponseApi)
  }

  it should "decode a failed job response" in {
    checkDecode("QueryJobResponseApi-failed.json", failedJobResponseApi)
  }

  behavior.of("QueryJobResponseApi")

  it should "decode a successful dry-run query job respoinse" in {
    checkDecode("DryRunQueryJobResponseApi-successful.json", successfulDryRunJobResponseApi)
  }

}
