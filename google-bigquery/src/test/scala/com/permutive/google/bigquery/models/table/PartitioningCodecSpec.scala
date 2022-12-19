package com.permutive.google.bigquery.models.table

import com.permutive.google.bigquery.models.table.PartitioningType.Day
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.literal._
import io.circe.syntax._

import scala.concurrent.duration.DurationDouble

class PartitioningCodecSpec extends AnyFlatSpec with Matchers {

  it should "encode partition expiration" in {
    Partitioning(Day, None, Some(1.second)).asJson should be(
      json"""{
               "type": "DAY",
               "field": null,
               "expirationMs": 1000
             }"""
    )
  }

  it should "decode partition expiration" in {
    json"""{
             "type": "DAY",
             "expirationMs": 1000
           }""".as[Partitioning] should be(Right(Partitioning(PartitioningType.Day, None, Some(1000.millis))))
  }
}
