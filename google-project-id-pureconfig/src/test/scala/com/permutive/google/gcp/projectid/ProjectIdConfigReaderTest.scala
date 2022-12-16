package com.permutive.google.gcp.projectid

import com.permutive.google.gcp.types.ProjectId
import munit.FunSuite
import pureconfig.generic.semiauto.deriveReader
import pureconfig.{ConfigReader, ConfigSource}

class ProjectIdConfigReaderTest extends FunSuite {
  test("Should read static project ID") {
    case class StaticTest(staticTest: ProjectIdConfig)
    implicit val reader: ConfigReader[StaticTest] = deriveReader
    val conf =
      ConfigSource.default.load[StaticTest]
    assertEquals(
      conf,
      Right(StaticTest(ProjectIdConfig.Static(ProjectId("foo-123"))))
    )
  }

  test("Should read gcp project ID") {
    case class GcpTest(gcpTest: ProjectIdConfig)
    implicit val reader: ConfigReader[GcpTest] = deriveReader
    val conf =
      ConfigSource.default.load[GcpTest]
    assertEquals(conf, Right(GcpTest(ProjectIdConfig.Gcp)))
  }
}
