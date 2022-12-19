package com.permutive.google.bigquery.rest.models.job.queryparameters

import com.permutive.google.bigquery.rest.models.ArbitraryInstances
import munit.ScalaCheckSuite
import org.scalacheck.Prop.forAll

class QueryParameterEncoderSpec
    extends ScalaCheckSuite
    with ArbitraryInstances {

  property("derive the correct QueryParameterEncoder") {
    forAll { (ss: List[String], name: String) =>
      val encoded = QueryParameterEncoder[List[String]].encode(name, ss)

      assertEquals(encoded.name, Some(name))
      assertEquals(encoded.parameterType, ParameterEncoder[List[String]].`type`)
      assertEquals(
        encoded.parameterValue,
        ParameterEncoder[List[String]].value(ss)
      )
    }
  }

}
