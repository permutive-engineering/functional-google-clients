package com.permutive.google.gcp.projectid

import com.permutive.google.gcp.types.ProjectId
import pureconfig._
import pureconfig.generic.derivation.default._

sealed trait ProjectIdConfig extends ProjectIdConfigBase derives ConfigReader

object ProjectIdConfig {
  implicit val projectIdReader: ConfigReader[ProjectId] =
    ConfigReader.fromStringOpt(ProjectId.fromString)

  case class Static(value: ProjectId) extends ProjectIdConfig
  case object Gcp extends ProjectIdConfig
}
