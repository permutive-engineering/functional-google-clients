package com.permutive.google.bigquery.rest.models

import com.permutive.google.bigquery.rest.models.job.queryparameters.{
  ArrayArrayCaseClass,
  ArrayCaseClass,
  NestedCaseClass,
  SimpleCaseClass,
}
import org.scalacheck.derive.{MkArbitrary, MkCogen}
import org.scalacheck.{Arbitrary, Cogen}

trait ArbitraryInstances {

  def deriveArbitrary[A](implicit derived: MkArbitrary[A]): Arbitrary[A] =
    derived.arbitrary

  def deriveCogen[A](implicit derived: MkCogen[A]): Cogen[A] =
    derived.cogen

  implicit val arbCost: Arbitrary[Cost] =
    Arbitrary(Arbitrary.arbBigDecimal.arbitrary.map(bd => Cost.fromBigDecimal(bd)))
  implicit val cogenCost: Cogen[Cost] =
    Cogen.bigDecimal.contramap(_.dollars)

  implicit val arbSimpleCaseClass: Arbitrary[SimpleCaseClass] = deriveArbitrary
  implicit val cogenSimpleCaseClass: Cogen[SimpleCaseClass]   = deriveCogen

  implicit val arbArrayCaseClass: Arbitrary[ArrayCaseClass] = deriveArbitrary
  implicit val cogenArrayCaseClass: Cogen[ArrayCaseClass]   = deriveCogen

  implicit val arbArrayArrayCaseClass: Arbitrary[ArrayArrayCaseClass] = deriveArbitrary
  implicit val cogenArrayArrayCaseClass: Cogen[ArrayArrayCaseClass]   = deriveCogen

  implicit val arbNestedCaseClass: Arbitrary[NestedCaseClass] = deriveArbitrary
  implicit val cogenNestedCaseClass: Cogen[NestedCaseClass]   = deriveCogen

}
