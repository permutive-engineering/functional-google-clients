package com.permutive.google.bigquery.rest.models.job

import com.permutive.google.bigquery.rest.models.TestData.Errors._
import com.permutive.google.bigquery.rest.models.api.ErrorProtoApi
import munit.FunSuite

class JobErrorSpec extends FunSuite {

  private def compareJobErrorToApi(error: JobError, api: ErrorProtoApi) = {
    assertEquals(error.reason, api.reason)
    assertEquals(error.location, api.location)
    assertEquals(error.message, api.message)
  }

  test("JobError.one should correctly convert to a JobError") {
    val test = JobError.one(errorProtoApi3)
    compareJobErrorToApi(test, errorProtoApi3)
  }

  test(
    "correctly convert to a JobError retaining the first value if the list is empty or not supplied"
  ) {
    val res1 = JobError.many(errorProtoApi1, Some(List.empty))
    val res2 = JobError.many(errorProtoApi1, None)

    assertEquals(res1, res2)

    val err1 = res1.toList match {
      case head :: Nil => head
      case _ =>
        throw new RuntimeException(
          s"List was of incorrect length, expected length 1: ${res1.toList}"
        )
    }

    compareJobErrorToApi(err1, errorProtoApi1)
  }

  test("correctly convert to a JobError retaining only the list if supplied") {
    val res =
      JobError.many(errorProtoApi1, Some(List(errorProtoApi2, errorProtoApi3)))

    val (err1, err2) = res.toList match {
      case head :: second :: Nil => (head, second)
      case _ =>
        throw new RuntimeException(
          s"List was of incorrect length, expected length 2: ${res.toList}"
        )
    }

    compareJobErrorToApi(err1, errorProtoApi2)
    compareJobErrorToApi(err2, errorProtoApi3)
  }

  test(
    "correctly convert to a NonEmptyList of JobError if errors are supplied"
  ) {
    val test = JobError.many(List(errorProtoApi1, errorProtoApi2))

    assert(test.isDefined)

    val res = test.get

    val (err1, err2) = res.toList match {
      case head :: second :: Nil => (head, second)
      case _ =>
        throw new RuntimeException(
          s"List was of incorrect length, expected length 2: ${res.toList}"
        )
    }

    compareJobErrorToApi(err1, errorProtoApi1)
    compareJobErrorToApi(err2, errorProtoApi2)
  }

  test("return None if no errors are supplied") {
    assertEquals(JobError.many(List.empty), None)
  }

}
