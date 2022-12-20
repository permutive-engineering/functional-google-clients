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

import cats.effect.kernel.Concurrent
import cats.syntax.applicative._
import com.permutive.google.gcp.types.ProjectId
import org.http4s.client.Client

private[projectid] trait ProjectIdConfigBase {
  def toProjectId[F[_]: Concurrent](httpClient: Client[F]): F[ProjectId] =
    this match {
      case ProjectIdConfig.Static(value) => value.pure[F]
      case ProjectIdConfig.Gcp =>
        ProjectIdResolver.fromInstanceMetadata(httpClient)
    }
}
