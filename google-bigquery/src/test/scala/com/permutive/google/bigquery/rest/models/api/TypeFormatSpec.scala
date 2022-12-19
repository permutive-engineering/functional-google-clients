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

package com.permutive.google.bigquery.rest.models.api

import com.permutive.google.bigquery.rest.models.api.TypeFormat._
import io.circe.Json
import io.circe.syntax._
import munit.ScalaCheckSuite
import org.scalacheck.{Arbitrary, Test}
import org.scalacheck.Prop.forAll

class TypeFormatSpec extends ScalaCheckSuite {
  override def scalaCheckTestParameters: Test.Parameters =
    Test.Parameters.default.withMinSuccessfulTests(100)

  property(
    "successfully decode from a valid long as a string, positive or negative"
  ) {
    forAll { l: Long =>
      val test = Json.fromString(l.toString).as[Int64Value]

      assert(test.isRight)
      val Right(res) = test

      assertEquals(res, Int64Value(l))
    }
  }

  implicit val arbInt64Value: Arbitrary[Int64Value] =
    Arbitrary(Arbitrary.arbLong.arbitrary.map(Int64Value(_)))

  property(
    "successfully encode to a valid long as a string, positive or negative"
  ) {
    forAll { l: Int64Value =>
      val test = l.asJson

      assertEquals(test, Json.fromString(l.value.toString))
    }
  }

  property(
    "successfully decode from a valid long as a string, positive or negative"
  ) {
    forAll { l: Long =>
      val test = Json.fromString(l.toString).as[UInt64Value]

      assert(test.isRight)
      val Right(res) = test

      assertEquals(res, UInt64Value(l))
    }
  }

  implicit val arbUInt64Value: Arbitrary[UInt64Value] =
    Arbitrary(Arbitrary.arbLong.arbitrary.map(UInt64Value(_)))

  property(
    "successfully encode to a valid long as a string, positive or negative"
  ) {
    forAll { l: UInt64Value =>
      val test = l.asJson

      assertEquals(test, Json.fromString(l.value.toString))
    }
  }

}
