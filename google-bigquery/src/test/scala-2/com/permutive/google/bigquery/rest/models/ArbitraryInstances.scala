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

import com.permutive.google.bigquery.rest.models.job.queryparameters.{
  ArrayArrayCaseClass,
  ArrayCaseClass,
  NestedCaseClass,
  SimpleCaseClass
}
import org.scalacheck.derive.{MkArbitrary, MkCogen}
import org.scalacheck.{Arbitrary, Cogen}

trait ArbitraryInstances {

  def deriveArbitrary[A](implicit derived: MkArbitrary[A]): Arbitrary[A] =
    derived.arbitrary

  def deriveCogen[A](implicit derived: MkCogen[A]): Cogen[A] =
    derived.cogen

  implicit val arbCost: Arbitrary[Cost] =
    Arbitrary(
      Arbitrary.arbBigDecimal.arbitrary.map(bd => Cost.fromBigDecimal(bd))
    )
  implicit val cogenCost: Cogen[Cost] =
    Cogen.bigDecimal.contramap(_.dollars)

  implicit val arbSimpleCaseClass: Arbitrary[SimpleCaseClass] = deriveArbitrary
  implicit val cogenSimpleCaseClass: Cogen[SimpleCaseClass] = deriveCogen

  implicit val arbArrayCaseClass: Arbitrary[ArrayCaseClass] = deriveArbitrary
  implicit val cogenArrayCaseClass: Cogen[ArrayCaseClass] = deriveCogen

  implicit val arbArrayArrayCaseClass: Arbitrary[ArrayArrayCaseClass] =
    deriveArbitrary
  implicit val cogenArrayArrayCaseClass: Cogen[ArrayArrayCaseClass] =
    deriveCogen

  implicit val arbNestedCaseClass: Arbitrary[NestedCaseClass] = deriveArbitrary
  implicit val cogenNestedCaseClass: Cogen[NestedCaseClass] = deriveCogen

}
