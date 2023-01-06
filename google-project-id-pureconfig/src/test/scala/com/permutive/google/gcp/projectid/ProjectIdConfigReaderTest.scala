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

package com.permutive.google.gcp.projectid

import com.permutive.google.gcp.types._
import munit.FunSuite
import pureconfig.{ConfigReader, ConfigSource}

class ProjectIdConfigReaderTest extends FunSuite {
  test("Should read static project ID") {
    case class StaticTest(staticTest: ProjectIdConfig)
    implicit val reader: ConfigReader[StaticTest] =
      ConfigReader
        .fromCursor(
          _.fluent
            .at("static-test")
            .cursor
            .flatMap(ConfigReader[ProjectIdConfig].from)
        )
        .map(StaticTest(_))

    val id: ProjectId = projectId"foo-123"

    val conf =
      ConfigSource.default.load[StaticTest]
    assertEquals(
      conf,
      Right(StaticTest(ProjectIdConfig.Static(id)))
    )
  }

  test("Should read gcp project ID") {
    case class GcpTest(gcpTest: ProjectIdConfig)
    implicit val reader: ConfigReader[GcpTest] = ConfigReader
      .fromCursor(
        _.fluent
          .at("gcp-test")
          .cursor
          .flatMap(ConfigReader[ProjectIdConfig].from)
      )
      .map(GcpTest(_))

    val conf =
      ConfigSource.default.load[GcpTest]
    assertEquals(conf, Right(GcpTest(ProjectIdConfig.Gcp)))
  }
}
