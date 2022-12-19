package com.permutive.google.bigquery.rest.models.job.queryparameters

import cats.data.{Chain, NonEmptyChain, NonEmptyList}
import cats.laws.discipline.arbitrary._
import cats.syntax.all._
import cats.{Foldable, Functor}
import com.permutive.google.bigquery.models.SQLType
import com.permutive.google.bigquery.rest.models.ArbitraryInstances
import org.scalacheck.Arbitrary
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ParameterEncoderSpec
    extends AnyFlatSpec
    with Matchers
    with TypeCheckedTripleEquals
    with ScalaCheckDrivenPropertyChecks
    with ArbitraryInstances {

  behavior.of("ParameterEncoder.intParameterEncoder")

  it should "properly encode all Int" in {
    forAll { i: Int =>
      ParameterEncoder[Int].`type` should ===(QueryParameterType(SQLType.Int64, None, None))
      ParameterEncoder[Int].value(i) should ===(QueryParameterValue(Some(i.toString), None, None))
    }
  }

  behavior.of("ParameterEncoder.stringParameterEncoder")

  it should "properly encode all String" in {
    forAll { i: String =>
      ParameterEncoder[String].`type` should ===(QueryParameterType(SQLType.String, None, None))
      ParameterEncoder[String].value(i) should ===(QueryParameterValue(Some(i), None, None))
    }
  }

  behavior.of("ParameterEncoder.doubleParameterEncoder")

  it should "properly encode all Double" in {
    forAll { i: Double =>
      ParameterEncoder[Double].`type` should ===(QueryParameterType(SQLType.Float64, None, None))
      ParameterEncoder[Double].value(i) should ===(QueryParameterValue(Some(i.toString), None, None))
    }
  }

  behavior.of("ParameterEncoder.booleanParameterEncoder")

  it should "properly encode all Boolean" in {
    forAll { i: Boolean =>
      ParameterEncoder[Boolean].`type` should ===(QueryParameterType(SQLType.Boolean, None, None))
      ParameterEncoder[Boolean].value(i) should ===(QueryParameterValue(Some(i.toString), None, None))
    }
  }

  behavior.of("ParameterEncoder.foldableParameterEncoder")

  def runTestArray[F[_]: Functor: Foldable, A: ParameterEncoder](
    parameterEncoder: ParameterEncoder[F[A]],
  )(implicit arb: Arbitrary[F[A]]): Assertion = {
    val expectedType =
      QueryParameterType(
        SQLType.Array,
        Some(ParameterEncoder[A].`type`),
        None,
      )

    forAll { as: F[A] =>
      val expectedValue =
        QueryParameterValue(
          None,
          Some(as.map(ParameterEncoder[A].value).toList),
          None,
        )

      parameterEncoder.`type` should ===(expectedType)
      parameterEncoder.value(as) should ===(expectedValue)
    }
  }

  def testArray[F[_]: Foldable: Functor, A: ParameterEncoder](implicit arb: Arbitrary[F[A]]): Assertion =
    runTestArray[F, A](implicitly)

  def testList[A: ParameterEncoder](implicit arb: Arbitrary[List[A]]): Assertion =
    runTestArray[List, A](ParameterEncoder.listParameterEncoder)

  it should "properly encode all List[String]" in {
    testArray[List, String]
    testList[String]
  }

  it should "properly encode all NonEmptyList[String]" in {
    testArray[NonEmptyList, String]
  }

  it should "properly encode all Chain[Int]" in {
    testArray[Chain, Int]
  }

  // Possibly undesired?
  it should "properly encode all Option[String]" in {
    testArray[Option, String]
  }

  it should "properly encode all NonEmptyChain[Boolean]" in {
    testArray[NonEmptyChain, Boolean]
  }

  behavior.of("ParameterEncoder.deriveEncoder")

  def testCaseClass[A: Arbitrary: ParameterEncoder](
    expectedType: QueryParameterType,
    expectedStructValues: A => ListMapLike[String, QueryParameterValue],
  ): Assertion =
    forAll { a: A =>
      ParameterEncoder[A].`type` should ===(expectedType)

      val expectedQueryParameterValue =
        QueryParameterValue(
          None,
          None,
          Some(expectedStructValues(a)),
        )

      ParameterEncoder[A].value(a) should ===(expectedQueryParameterValue)
    }

  val expectedSimplCaseClassType =
    QueryParameterType(
      SQLType.Struct,
      None,
      Some(
        List(
          StructType(
            Some("foo"),
            QueryParameterType.singular(SQLType.String),
          ),
          StructType(
            Some("bar"),
            QueryParameterType.singular(SQLType.String),
          ),
          StructType(
            Some("baz"),
            QueryParameterType.singular(SQLType.Int64),
          ),
          StructType(
            Some("qux"),
            QueryParameterType.singular(SQLType.Float64),
          ),
          StructType(
            Some("blib"),
            QueryParameterType.singular(SQLType.Boolean),
          ),
        ),
      ),
    )

  def expectedSimpleQueryParameterValues(simple: SimpleCaseClass): ListMapLike[String, QueryParameterValue] =
    ListMapLike(
      List(
        "foo"  -> QueryParameterValue(value = Some(simple.foo), None, None),
        "bar"  -> QueryParameterValue(value = Some(simple.bar), None, None),
        "baz"  -> QueryParameterValue(value = Some(simple.baz.toString), None, None),
        "qux"  -> QueryParameterValue(value = Some(simple.qux.toString), None, None),
        "blib" -> QueryParameterValue(value = Some(simple.blib.toString), None, None),
      ),
    )

  it should "properly encode for all SimpleCaseClass" in {
    testCaseClass[SimpleCaseClass](
      expectedSimplCaseClassType,
      expectedSimpleQueryParameterValues,
    )
  }

  implicit val parameterEncoderArrayCaseClass: ParameterEncoder[ArrayCaseClass] = ParameterEncoder.deriveEncoder

  val expectedArrayCaseClassType =
    QueryParameterType(
      SQLType.Struct,
      None,
      Some(
        List(
          StructType(
            Some("foo"),
            QueryParameterType(
              SQLType.String,
              None,
              None,
            ),
          ),
          StructType(
            Some("bar"),
            QueryParameterType(
              SQLType.Array,
              Some(QueryParameterType(SQLType.String, None, None)),
              None,
            ),
          ),
        ),
      ),
    )

  def expectedArrayCaseClassValues(array: ArrayCaseClass): ListMapLike[String, QueryParameterValue] =
    ListMapLike(
      List(
        "foo" -> QueryParameterValue(value = Some(array.foo), None, None),
        "bar" -> QueryParameterValue(
          None,
          Some(array.bar.map(s => QueryParameterValue(value = Some(s), None, None))),
          None,
        ),
      ),
    )

  it should "properly encode for all ArrayCaseClass" in {
    testCaseClass[ArrayCaseClass](
      expectedArrayCaseClassType,
      expectedArrayCaseClassValues,
    )
  }

  val expectedArrayArrayCaseClassType =
    QueryParameterType(
      SQLType.Struct,
      None,
      Some(
        List(
          StructType(
            Some("foo"),
            QueryParameterType(
              SQLType.Array,
              Some(
                QueryParameterType(
                  SQLType.Array,
                  Some(QueryParameterType(SQLType.Int64, None, None)),
                  None,
                ),
              ),
              None,
            ),
          ),
        ),
      ),
    )

  def expectedArrayArrayCaseClassValues(array: ArrayArrayCaseClass): ListMapLike[String, QueryParameterValue] =
    ListMapLike(
      List(
        "foo" -> QueryParameterValue(
          None,
          Some(
            array.foo.map(is =>
              QueryParameterValue(
                None,
                Some(is.map(i => QueryParameterValue(value = Some(i.toString), None, None))),
                None,
              ),
            ),
          ),
          None,
        ),
      ),
    )

  it should "properly encode for all ArrayArrayCaseClass" in {
    testCaseClass[ArrayArrayCaseClass](
      expectedArrayArrayCaseClassType,
      expectedArrayArrayCaseClassValues,
    )
  }

  val expectedNestedCaseClassType: QueryParameterType =
    QueryParameterType(
      SQLType.Struct,
      None,
      Some(
        List(
          StructType(
            Some("foo"),
            QueryParameterType(
              SQLType.Int64,
              None,
              None,
            ),
          ),
          StructType(
            Some("bar"),
            expectedSimplCaseClassType,
          ),
          StructType(
            Some("baz"),
            expectedArrayCaseClassType,
          ),
        ),
      ),
    )

  def expectedNestedCaseClassValues(nested: NestedCaseClass): ListMapLike[String, QueryParameterValue] =
    ListMapLike(
      List(
        "foo" -> QueryParameterValue(Some(nested.foo.toString), None, None),
        "bar" -> QueryParameterValue(None, None, Some(expectedSimpleQueryParameterValues(nested.bar))),
        "baz" -> QueryParameterValue(None, None, Some(expectedArrayCaseClassValues(nested.baz))),
      ),
    )

  it should "properly encode for all NestedCaseClass" in {
    testCaseClass[NestedCaseClass](
      expectedNestedCaseClassType,
      expectedNestedCaseClassValues,
    )
  }

}
