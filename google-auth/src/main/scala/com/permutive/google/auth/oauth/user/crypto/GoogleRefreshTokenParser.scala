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

package com.permutive.google.auth.oauth.user.crypto

import java.nio.file.{Files, Path}

import cats.effect.{Resource, Sync}
import cats.syntax.all._
import com.permutive.google.auth.oauth.user.models.NewTypes._

import scala.compat.java8.OptionConverters._

object GoogleRefreshTokenParser {

  final def parse[F[_]](path: Path)(implicit F: Sync[F]): F[RefreshToken] =
    linesResource(path).use { lines =>
      for {
        lineO <- F.delay(lines.findFirst().asScala)
        line <- F.fromOption(lineO, EmptyRefreshTokenFileException(path))
      } yield RefreshToken(line.trim)
    }

  private def linesResource[F[_]](path: Path)(implicit
      F: Sync[F]
  ): Resource[F, java.util.stream.Stream[String]] =
    Resource.fromAutoCloseable(F.delay(Files.lines(path)))

  case class EmptyRefreshTokenFileException(path: Path)
      extends RuntimeException(
        s"Attempted to parse a Google refresh token but the file was empty: $path"
      )

}
