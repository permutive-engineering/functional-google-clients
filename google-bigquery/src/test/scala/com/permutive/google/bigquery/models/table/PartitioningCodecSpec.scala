package com.permutive.google.bigquery.models.table

import com.permutive.google.bigquery.models.table.PartitioningType.Day
import io.circe.literal._
import io.circe.syntax._
import munit.FunSuite

import scala.concurrent.duration.DurationDouble

class PartitioningCodecSpec extends FunSuite {

  test("encode partition expiration") {
    assertEquals(
      Partitioning(Day, None, Some(1.second)).asJson,
      json"""{
               "type": "DAY",
               "field": null,
               "expirationMs": 1000
             }"""
    )
  }

  test("decode partition expiration") {
    assertEquals(
      json"""{
             "type": "DAY",
             "expirationMs": 1000
           }""".as[Partitioning],
      Right(Partitioning(PartitioningType.Day, None, Some(1000.millis)))
    )
  }
}
