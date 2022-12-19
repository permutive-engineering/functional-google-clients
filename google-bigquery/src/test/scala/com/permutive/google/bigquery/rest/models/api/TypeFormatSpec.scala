package com.permutive.google.bigquery.rest.models.api

import com.permutive.google.bigquery.rest.models.api.TypeFormat._
import io.circe.Json
import io.circe.syntax._
import org.scalacheck.Arbitrary
import org.scalactic.TypeCheckedTripleEquals
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TypeFormatSpec
    extends AnyFlatSpec
    with Matchers
    with TypeCheckedTripleEquals
    with ScalaCheckDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 100)

  behavior.of("Int64Value")

  it should "successfully decode from a valid long as a string, positive or negative" in {
    forAll { l: Long =>
      val test = Json.fromString(l.toString).as[Int64Value]

      test shouldBe Symbol("right")
      val Right(res) = test

      res should ===(Int64Value(l))
    }
  }

  implicit val arbInt64Value: Arbitrary[Int64Value] =
    Arbitrary(Arbitrary.arbLong.arbitrary.map(Int64Value(_)))

  it should "successfully encode to a valid long as a string, positive or negative" in {
    forAll { l: Int64Value =>
      val test = l.asJson

      test should ===(Json.fromString(l.value.toString))
    }
  }

  behavior.of("UInt64Value")

  it should "successfully decode from a valid long as a string, positive or negative" in {
    forAll { l: Long =>
      val test = Json.fromString(l.toString).as[UInt64Value]

      test shouldBe Symbol("right")
      val Right(res) = test

      res should ===(UInt64Value(l))
    }
  }

  implicit val arbUInt64Value: Arbitrary[UInt64Value] =
    Arbitrary(Arbitrary.arbLong.arbitrary.map(UInt64Value(_)))

  it should "successfully encode to a valid long as a string, positive or negative" in {
    forAll { l: UInt64Value =>
      val test = l.asJson

      test should ===(Json.fromString(l.value.toString))
    }
  }

}
