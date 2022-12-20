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

import cats.Show
import cats.kernel.{CommutativeGroup, Order}
import com.permutive.google.bigquery.models.NewTypes.Location
import java.math.MathContext

sealed abstract case class Cost(dollars: BigDecimal) {

  override def toString: String = Show[Cost].show(this)

}

object Cost {

  def apply(bytesProcessed: Long): Cost =
    apply(bytesProcessed, None)

  def apply(bytesProcessed: Long, location: Location): Cost =
    apply(bytesProcessed, Some(location))

  def apply(bytesProcessed: Long, location: Option[Location]): Cost = {
    val costPerTB: Double =
      location
        .flatMap(l => locationCostPerTB.get(l.value.toLowerCase))
        .getOrElse(defaultCostPerTB)

    val processedTB = bytesProcessed.toDouble / bytesPerTB

    fromBigDecimal(costPerTB * processedTB)
  }

  // Exposed for testing purposes (to create an Arbitrary)
  private[models] def fromBigDecimal(x: BigDecimal) =
    new Cost(x) {}

  // Documentation for cost: https://cloud.google.com/bigquery/pricing#on_demand_pricing

  // BigQuery refer to `terabyte` and `TB` but really work in `tebibyte` and `TiB`. Stating `1TB = 2^40 bytes`
  // Search for "Note: storage is" and "gibibyte" in the above doc.
  private[this] val bytesPerTB = math.pow(2d, 40d).toLong

  private[this] val defaultCostPerTB = 5d

  // LAST UPDATED: 2019-09-03
  private[this] val locationCostPerTB: Map[String, Double] =
    (List(
      Location.US -> 5d,
      Location.EU -> 5d,
      Location.USWest2 -> 10.35, // Los Angeles
      Location.NorthAmericaNorthEast1 -> 7.60, // Montréal
      Location.UsEast4 -> 6.45, // Northern Virginia
      Location.SouthAmericaEast1 -> 13.85, // São Paulo
      Location.EuropeNorth1 -> 9.20, // Finland
      Location.EuropeWest2 -> 9.35, // London
      Location.EuropeWest6 -> 9.20, // Zürich
      Location.AsiaEast2 -> 11.25, // Hong Kong
      Location.AsiaSouth1 -> 8.90, // Mumbai
      Location.AsiaEast1 -> 9.40, // Taiwan
      Location.AsiaNorthEast1 -> 8.55, // Tokyo
      Location.AsiaSouthEast1 -> 10.75, // Singapore
      Location.AustraliaSouthEast1 -> 9.55 // Sydney
    ) ::: List(
      Location.AsiaNorthEast2 -> defaultCostPerTB // Osaka, not listed in pricing calculator
    )).map { case (l, c) => (l.value.toLowerCase, c) }.toMap

  implicit val commutativeGroup: CommutativeGroup[Cost] =
    new CommutativeGroup[Cost] {
      override val empty = new Cost(0) {}

      // We have to provide the context for 2.13 because of https://github.com/scala/bug/issues/11590.
      override def combine(x: Cost, y: Cost) =
        new Cost(
          BigDecimal(
            x.dollars.bigDecimal
              .add(y.dollars.bigDecimal, MathContext.UNLIMITED)
          )
        ) {}

      override def inverse(a: Cost) = new Cost(-a.dollars) {}
    }

  implicit val order: Order[Cost] = Order.by[Cost, BigDecimal](_.dollars)

  implicit val show: Show[Cost] = Show.show(c => s"$$${c.dollars}")

}
