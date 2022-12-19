package com.permutive.google.bigquery.models.schema

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class AccessSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  behavior.of("AccessApi.decode")

  case class Collection(access: List[Access])

  implicit val niceStringArb: Arbitrary[String] = Arbitrary(Gen.alphaNumStr)

  it should "encode and decode an access request with an email" in {
    forAll { (role: String, email: String) =>
      val json =
        s"""{
           |  "role" : "$role",
           |  "userByEmail" : "$email"
           |}""".stripMargin

      val access = Access.userByEmail(role, email)

      access.asJson.spaces2 shouldEqual json
      parse(json).flatMap(_.as[Access]) shouldEqual Right(access)
    }
  }

  it should "encode and decode an access request with a group email" in {
    forAll { (role: String, email: String) =>
      val json =
        s"""{
           |  "role" : "$role",
           |  "groupByEmail" : "$email"
           |}""".stripMargin

      val access = Access.groupByEmail(role, email)

      access.asJson.spaces2 shouldEqual json
      parse(json).flatMap(_.as[Access]) shouldEqual Right(access)
    }
  }

  it should "encode and decode an access request with a domain" in {
    forAll { (role: String, domain: String) =>
      val json =
        s"""{
           |  "role" : "$role",
           |  "domain" : "$domain"
           |}""".stripMargin

      val access = Access.domain(role, domain)

      access.asJson.spaces2 shouldEqual json
      parse(json).flatMap(_.as[Access]) shouldEqual Right(access)
    }
  }

  it should "encode and decode an access request with a special group" in {
    forAll { (role: String, group: String) =>
      val json =
        s"""{
           |  "role" : "$role",
           |  "specialGroup" : "$group"
           |}""".stripMargin

      val access = Access.specialGroup(role, group)

      access.asJson.spaces2 shouldEqual json
      parse(json).flatMap(_.as[Access]) shouldEqual Right(access)
    }
  }

  it should "encode an access request with an IAM member" in {
    forAll { (role: String, member: String) =>
      val json =
        s"""{
           |  "role" : "$role",
           |  "iamMember" : "$member"
           |}""".stripMargin

      val access = Access.iamMember(role, member)

      access.asJson.spaces2 shouldEqual json
      parse(json).flatMap(_.as[Access]) shouldEqual Right(access)
    }
  }

  it should "encode and decode a collection" in {
    forAll {
      (
        role: String,
        email1: String,
        email2: String,
        domain: String,
        group: String,
        iamMember: String,
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
            Access.iamMember(role, iamMember),
          ),
        )

        collection.asJson.spaces2 shouldEqual json

        parse(json).flatMap(_.as[Collection]) shouldEqual Right(collection)
    }
  }
}
