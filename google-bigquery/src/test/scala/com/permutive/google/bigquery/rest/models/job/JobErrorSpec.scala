package com.permutive.google.bigquery.rest.models.job

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.Assertion
import com.permutive.google.bigquery.rest.models.TestData.Errors._
import com.permutive.google.bigquery.rest.models.api.ErrorProtoApi
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JobErrorSpec extends AnyFlatSpec with Matchers with TypeCheckedTripleEquals {

  private def compareJobErrorToApi(error: JobError, api: ErrorProtoApi): Assertion = {
    error.reason should ===(api.reason)
    error.location should ===(api.location)
    error.message should ===(api.message)
  }

  "JobError.one" should "correctly convert to a JobError" in {
    val test = JobError.one(errorProtoApi3)
    compareJobErrorToApi(test, errorProtoApi3)
  }

  behavior.of("JobError.many(ErrorProtoApi, Option[List[ErrorProtoApi]])")

  it should "correctly convert to a JobError retaining the first value if the list is empty or not supplied" in {
    val res1 = JobError.many(errorProtoApi1, Some(List.empty))
    val res2 = JobError.many(errorProtoApi1, None)

    res1 should ===(res2)

    val err1 = res1.toList match {
      case head :: Nil => head
      case _           => throw new RuntimeException(s"List was of incorrect length, expected length 1: ${res1.toList}")
    }

    compareJobErrorToApi(err1, errorProtoApi1)
  }

  it should "correctly convert to a JobError retaining only the list if supplied" in {
    val res = JobError.many(errorProtoApi1, Some(List(errorProtoApi2, errorProtoApi3)))

    val (err1, err2) = res.toList match {
      case head :: second :: Nil => (head, second)
      case _                     => throw new RuntimeException(s"List was of incorrect length, expected length 2: ${res.toList}")
    }

    compareJobErrorToApi(err1, errorProtoApi2)
    compareJobErrorToApi(err2, errorProtoApi3)
  }

  behavior.of("JobError.many(List[ErrorProtoApi])")

  it should "correctly convert to a NonEmptyList of JobError if errors are supplied" in {
    val test = JobError.many(List(errorProtoApi1, errorProtoApi2))

    test shouldBe defined

    val res = test.get

    val (err1, err2) = res.toList match {
      case head :: second :: Nil => (head, second)
      case _                     => throw new RuntimeException(s"List was of incorrect length, expected length 2: ${res.toList}")
    }

    compareJobErrorToApi(err1, errorProtoApi1)
    compareJobErrorToApi(err2, errorProtoApi2)
  }

  it should "return None if no errors are supplied" in {
    JobError.many(List.empty) shouldBe None
  }

}
