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
