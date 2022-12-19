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
