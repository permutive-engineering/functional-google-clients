package com.permutive.google.gcp.projectid

import com.permutive.google.gcp.types.ProjectId
import pureconfig.ConfigReader
import pureconfig.generic.auto._
import pureconfig.generic.semiauto._

sealed trait ProjectIdConfig extends ProjectIdConfigBase

object ProjectIdConfig {
  implicit val projectIdReader: ConfigReader[ProjectId] =
    ConfigReader.fromStringOpt(ProjectId.fromString)

  case class Static(value: ProjectId) extends ProjectIdConfig
  case object Gcp extends ProjectIdConfig

  implicit val reader: ConfigReader[ProjectIdConfig] =
    deriveReader[ProjectIdConfig]
}
