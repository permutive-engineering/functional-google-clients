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
