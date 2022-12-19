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

package com.permutive.google.bigquery.models.schema

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen}

class AccessSpec extends ScalaCheckSuite {
  case class Collection(access: List[Access])

  implicit val niceStringArb: Arbitrary[String] = Arbitrary(Gen.alphaNumStr)

  property("encode and decode an access request with an email") {
    forAll { (role: String, email: String) =>
      val json =
        s"""{
           |  "role" : "$role",
           |  "userByEmail" : "$email"
           |}""".stripMargin

      val access = Access.userByEmail(role, email)

      assertEquals(access.asJson.spaces2, json)
      assertEquals(parse(json).flatMap(_.as[Access]), Right(access))
    }
  }

  property("encode and decode an access request with a group email") {
    forAll { (role: String, email: String) =>
      val json =
        s"""{
           |  "role" : "$role",
           |  "groupByEmail" : "$email"
           |}""".stripMargin

      val access = Access.groupByEmail(role, email)

      assertEquals(access.asJson.spaces2, json)
      assertEquals(parse(json).flatMap(_.as[Access]), Right(access))
    }
  }

  property("encode and decode an access request with a domain") {
    forAll { (role: String, domain: String) =>
      val json =
        s"""{
           |  "role" : "$role",
           |  "domain" : "$domain"
           |}""".stripMargin

      val access = Access.domain(role, domain)

      assertEquals(access.asJson.spaces2, json)
      assertEquals(parse(json).flatMap(_.as[Access]), Right(access))
    }
  }

  property("encode and decode an access request with a special group") {
    forAll { (role: String, group: String) =>
      val json =
        s"""{
           |  "role" : "$role",
           |  "specialGroup" : "$group"
           |}""".stripMargin

      val access = Access.specialGroup(role, group)

      assertEquals(access.asJson.spaces2, json)
      assertEquals(parse(json).flatMap(_.as[Access]), Right(access))
    }
  }

  property("encode an access request with an IAM member") {
    forAll { (role: String, member: String) =>
      val json =
        s"""{
           |  "role" : "$role",
           |  "iamMember" : "$member"
           |}""".stripMargin

      val access = Access.iamMember(role, member)

      assertEquals(access.asJson.spaces2, json)
      assertEquals(parse(json).flatMap(_.as[Access]), Right(access))
    }
  }

  property("encode and decode a collection") {
    forAll {
      (
          role: String,
          email1: String,
          email2: String,
          domain: String,
          group: String,
          iamMember: String
      ) =>
        val json =
          s"""{
             |  "access" : [
             |    {
             |      "role" : "$role",
             |      "userByEmail" : "$email1"
             |    },
             |    {
             |      "role" : "$role",
             |      "groupByEmail" : "$email2"
             |    },
             |    {
             |      "role" : "$role",
             |      "domain" : "$domain"
             |    },
             |    {
             |      "role" : "$role",
             |      "specialGroup" : "$group"
             |    },
             |    {
             |      "role" : "$role",
             |      "iamMember" : "$iamMember"
             |    }
             |  ]
             |}""".stripMargin

        val collection = Collection(
          List(
            Access.userByEmail(role, email1),
            Access.groupByEmail(role, email2),
            Access.domain(role, domain),
            Access.specialGroup(role, group),
            Access.iamMember(role, iamMember)
          )
        )

        assertEquals(collection.asJson.spaces2, json)

        assertEquals(parse(json).flatMap(_.as[Collection]), Right(collection))
    }
  }
}
