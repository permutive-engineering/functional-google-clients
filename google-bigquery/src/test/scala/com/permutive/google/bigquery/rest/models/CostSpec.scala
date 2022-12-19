package com.permutive.google.bigquery.rest.models

import cats.kernel.laws.discipline.{CommutativeGroupTests, OrderTests}
import com.permutive.google.bigquery.models.NewTypes.Location
import com.permutive.testutils.CatsLawsSuite
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor3}

class CostSpec extends CatsLawsSuite with TableDrivenPropertyChecks with ArbitraryInstances {

  checkLaws("CommutativeGroup[Cost]", CommutativeGroupTests[Cost].commutativeGroup)
  checkLaws("OrderTests[Cost]", OrderTests[Cost].order)

  // Also TB for BQ
  val tebiByte = 1099511627776L

  behavior.of("Cost")

  val bytesLocationExpectedCost: TableFor3[Long, Option[Location], Double] =
    Table(
      ("bytes", "location", "expeced cost"),
      (tebiByte, Some(Location.US), 5d),
      (tebiByte, None, 5d),
      (tebiByte, Some(Location.EU), 5d),
      (tebiByte, Some(Location.SaoPaulo), 13.85),
      (tebiByte * 2, Some(Location.SaoPaulo), 13.85 * 2),
      ((tebiByte * 1.5).toLong, None, 5d * 1.5),
    )

  it should "produce the correct cost for some known values" in {
    forAll(bytesLocationExpectedCost) { case (bytes, location, cost) =>
      Cost(bytes, location) should matchPattern {
        case Cost(bd) if bd.toDouble == cost =>
      }
    }
  }

}
