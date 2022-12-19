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

package com.permutive.google.bigquery.rest.models

import cats.kernel.laws.discipline.{CommutativeGroupTests, OrderTests}
import com.permutive.google.bigquery.models.NewTypes.Location
import munit.DisciplineSuite

class CostSpec extends DisciplineSuite with ArbitraryInstances {

  checkAll(
    "CommutativeGroup[Cost]",
    CommutativeGroupTests[Cost].commutativeGroup
  )
  checkAll("OrderTests[Cost]", OrderTests[Cost].order)

  // Also TB for BQ
  val tebiByte = 1099511627776L

  val bytesLocationExpectedCost: List[(Long, Option[Location], Double)] =
    List(
      (tebiByte, Some(Location.US), 5d),
      (tebiByte, None, 5d),
      (tebiByte, Some(Location.EU), 5d),
      (tebiByte, Some(Location.SaoPaulo), 13.85),
      (tebiByte * 2, Some(Location.SaoPaulo), 13.85 * 2),
      ((tebiByte * 1.5).toLong, None, 5d * 1.5)
    )

  test("produce the correct cost for some known values") {
    bytesLocationExpectedCost.foreach { case (bytes, location, cost) =>
      assert(Cost(bytes, location) match {
        case Cost(bd) if bd.toDouble == cost => true
        case _                               => false
      })
    }
  }

}
