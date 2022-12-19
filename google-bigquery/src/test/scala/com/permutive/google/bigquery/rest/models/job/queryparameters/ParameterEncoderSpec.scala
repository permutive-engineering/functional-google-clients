package com.permutive.google.bigquery.rest.models.job.queryparameters

import cats.data.{Chain, NonEmptyChain, NonEmptyList}
import cats.laws.discipline.arbitrary._
import cats.syntax.all._
import cats.{Foldable, Functor}
import com.permutive.google.bigquery.models.SQLType
import com.permutive.google.bigquery.rest.models.ArbitraryInstances
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll

class ParameterEncoderSpec extends ScalaCheckSuite with ArbitraryInstances {

  property("properly encode all Int") {
    forAll { i: Int =>
      assertEquals(
        ParameterEncoder[Int].`type`,
        QueryParameterType(SQLType.Int64, None, None)
      )
      assertEquals(
        ParameterEncoder[Int].value(i),
        QueryParameterValue(Some(i.toString), None, None)
      )
    }
  }

  property("properly encode all String") {
    forAll { i: String =>
      assertEquals(
        ParameterEncoder[String].`type`,
        QueryParameterType(SQLType.String, None, None)
      )
      assertEquals(
        ParameterEncoder[String].value(i),
        QueryParameterValue(Some(i), None, None)
      )
    }
  }

  property("properly encode all Double") {
    forAll { i: Double =>
      assertEquals(
        ParameterEncoder[Double].`type`,
        QueryParameterType(SQLType.Float64, None, None)
      )
      assertEquals(
        ParameterEncoder[Double].value(i),
        QueryParameterValue(Some(i.toString), None, None)
      )
    }
  }

  property("properly encode all Boolean") {
    forAll { i: Boolean =>
      assertEquals(
        ParameterEncoder[Boolean].`type`,
        QueryParameterType(SQLType.Boolean, None, None)
      )
      assertEquals(
        ParameterEncoder[Boolean].value(i),
        QueryParameterValue(Some(i.toString), None, None)
      )
    }
  }

  def runTestArray[F[_]: Functor: Foldable, A: ParameterEncoder](
      parameterEncoder: ParameterEncoder[F[A]]
  )(implicit arb: Arbitrary[F[A]]) = {
    val expectedType =
      QueryParameterType(
        SQLType.Array,
        Some(ParameterEncoder[A].`type`),
        None
      )

    forAll { as: F[A] =>
      val expectedValue =
        QueryParameterValue(
          None,
          Some(as.map(ParameterEncoder[A].value).toList),
          None
        )

      assertEquals(parameterEncoder.`type`, expectedType)
      assertEquals(parameterEncoder.value(as), expectedValue)
    }
  }

  def testArray[F[_]: Foldable: Functor, A: ParameterEncoder](implicit
      arb: Arbitrary[F[A]]
  ) =
    runTestArray[F, A](implicitly)

  def testList[A: ParameterEncoder](implicit
      arb: Arbitrary[List[A]]
  ) =
    runTestArray[List, A](ParameterEncoder.listParameterEncoder)

  property("properly encode all List[String]") {
    testArray[List, String]
    testList[String]
  }

  property("properly encode all NonEmptyList[String]") {
    testArray[NonEmptyList, String]
  }

  property("properly encode all Chain[Int]") {
    testArray[Chain, Int]
  }

  // Possibly undesired?
  property("properly encode all Option[String]") {
    testArray[Option, String]
  }

  property("properly encode all NonEmptyChain[Boolean]") {
    testArray[NonEmptyChain, Boolean]
  }

  def testCaseClass[A: Arbitrary: ParameterEncoder](
      expectedType: QueryParameterType,
      expectedStructValues: A => ListMapLike[String, QueryParameterValue]
  ) =
    forAll { a: A =>
      assertEquals(ParameterEncoder[A].`type`, expectedType)

      val expectedQueryParameterValue =
        QueryParameterValue(
          None,
          None,
          Some(expectedStructValues(a))
        )

      assertEquals(ParameterEncoder[A].value(a), expectedQueryParameterValue)
    }

  val expectedSimplCaseClassType =
    QueryParameterType(
      SQLType.Struct,
      None,
      Some(
        List(
          StructType(
            Some("foo"),
            QueryParameterType.singular(SQLType.String)
          ),
          StructType(
            Some("bar"),
            QueryParameterType.singular(SQLType.String)
          ),
          StructType(
            Some("baz"),
            QueryParameterType.singular(SQLType.Int64)
          ),
          StructType(
            Some("qux"),
            QueryParameterType.singular(SQLType.Float64)
          ),
          StructType(
            Some("blib"),
            QueryParameterType.singular(SQLType.Boolean)
          )
        )
      )
    )

  def expectedSimpleQueryParameterValues(
      simple: SimpleCaseClass
  ): ListMapLike[String, QueryParameterValue] =
    ListMapLike(
      List(
        "foo" -> QueryParameterValue(value = Some(simple.foo), None, None),
        "bar" -> QueryParameterValue(value = Some(simple.bar), None, None),
        "baz" -> QueryParameterValue(
          value = Some(simple.baz.toString),
          None,
          None
        ),
        "qux" -> QueryParameterValue(
          value = Some(simple.qux.toString),
          None,
          None
        ),
        "blib" -> QueryParameterValue(
          value = Some(simple.blib.toString),
          None,
          None
        )
      )
    )

  test("properly encode for all SimpleCaseClass") {
    testCaseClass[SimpleCaseClass](
      expectedSimplCaseClassType,
      expectedSimpleQueryParameterValues
    )
  }

  implicit val parameterEncoderArrayCaseClass
      : ParameterEncoder[ArrayCaseClass] = ParameterEncoder.deriveEncoder

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
              None
            )
          ),
          StructType(
            Some("bar"),
            QueryParameterType(
              SQLType.Array,
              Some(QueryParameterType(SQLType.String, None, None)),
              None
            )
          )
        )
      )
    )

  def expectedArrayCaseClassValues(
      array: ArrayCaseClass
  ): ListMapLike[String, QueryParameterValue] =
    ListMapLike(
      List(
        "foo" -> QueryParameterValue(value = Some(array.foo), None, None),
        "bar" -> QueryParameterValue(
          None,
          Some(
            array.bar.map(s => QueryParameterValue(value = Some(s), None, None))
          ),
          None
        )
      )
    )

  property("properly encode for all ArrayCaseClass") {
    testCaseClass[ArrayCaseClass](
      expectedArrayCaseClassType,
      expectedArrayCaseClassValues
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
                  None
                )
              ),
              None
            )
          )
        )
      )
    )

  def expectedArrayArrayCaseClassValues(
      array: ArrayArrayCaseClass
  ): ListMapLike[String, QueryParameterValue] =
    ListMapLike(
      List(
        "foo" -> QueryParameterValue(
          None,
          Some(
            array.foo.map(is =>
              QueryParameterValue(
                None,
                Some(
                  is.map(i =>
                    QueryParameterValue(value = Some(i.toString), None, None)
                  )
                ),
                None
              )
            )
          ),
          None
        )
      )
    )

  property("properly encode for all ArrayArrayCaseClass") {
    testCaseClass[ArrayArrayCaseClass](
      expectedArrayArrayCaseClassType,
      expectedArrayArrayCaseClassValues
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
              None
            )
          ),
          StructType(
            Some("bar"),
            expectedSimplCaseClassType
          ),
          StructType(
            Some("baz"),
            expectedArrayCaseClassType
          )
        )
      )
    )

  def expectedNestedCaseClassValues(
      nested: NestedCaseClass
  ): ListMapLike[String, QueryParameterValue] =
    ListMapLike(
      List(
        "foo" -> QueryParameterValue(Some(nested.foo.toString), None, None),
        "bar" -> QueryParameterValue(
          None,
          None,
          Some(expectedSimpleQueryParameterValues(nested.bar))
        ),
        "baz" -> QueryParameterValue(
          None,
          None,
          Some(expectedArrayCaseClassValues(nested.baz))
        )
      )
    )

  property("properly encode for all NestedCaseClass") {
    testCaseClass[NestedCaseClass](
      expectedNestedCaseClassType,
      expectedNestedCaseClassValues
    )
  }

}
