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

package com.permutive.google.gcp

import eu.timepit.refined.W
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.string.MatchesRegex

package object types {
  /* From https://cloud.google.com/resource-manager/docs/creating-managing-projects#before_you_begin:
   *
   * The project ID must be a unique string of 6 to 30 lowercase letters, digits, or hyphens. It must start with a
   * letter, and cannot have a trailing hyphen.
   */
  type ProjectIdRefinement = MatchesRegex[W.`"[a-z][a-z0-9-]{5,29}(?<!-)"`.T]
  type ProjectId = String Refined ProjectIdRefinement
  object ProjectId extends RefinedTypeOps[ProjectId, String]
}
