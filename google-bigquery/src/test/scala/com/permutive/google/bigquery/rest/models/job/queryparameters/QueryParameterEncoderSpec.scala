package com.permutive.google.bigquery.rest.models.job.queryparameters

import com.permutive.google.bigquery.rest.models.ArbitraryInstances
import org.scalactic.TypeCheckedTripleEquals
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class QueryParameterEncoderSpec
    extends AnyFlatSpec
    with Matchers
    with TypeCheckedTripleEquals
    with ScalaCheckDrivenPropertyChecks
    with ArbitraryInstances {

  behavior.of("QueryParameterEncoder.deriveEncoder")

  it should "derive the correct QueryParameterEncoder" in {
    forAll { (ss: List[String], name: String) =>
      val encoded = QueryParameterEncoder[List[String]].encode(name, ss)

      encoded.name should ===(Some(name))
      encoded.parameterType should ===(ParameterEncoder[List[String]].`type`)
      encoded.parameterValue should ===(ParameterEncoder[List[String]].value(ss))
    }
  }

}
