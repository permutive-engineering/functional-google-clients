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
