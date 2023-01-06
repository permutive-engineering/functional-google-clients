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

import java.nio.file.{Files, Path, Paths}

import cats.effect.IO
import com.permutive.google.auth.oauth.user.crypto.GoogleRefreshTokenParser.EmptyRefreshTokenFileException
import com.permutive.google.auth.oauth.user.models.NewTypes.RefreshToken
import munit.CatsEffectSuite

class GoogleRefreshTokenParserSpec extends CatsEffectSuite {

  private def resourcePath(fileName: String): Path =
    Paths.get(getClass.getResource(fileName).getFile)

  test("parse a valid file") {
    val path: Path = resourcePath("standard_refresh_token_file.txt")
    val expected: RefreshToken = RefreshToken("refresh-token")

    val parsed = GoogleRefreshTokenParser.parse[IO](path).unsafeRunSync()

    assertEquals(parsed, expected)
  }

  test("only include the first line of a file, ignoring others") {
    val path: Path = resourcePath("multi_line_refresh_token_file.txt")
    val expected: RefreshToken = RefreshToken("refresh-token")

    val parsed = GoogleRefreshTokenParser.parse[IO](path).unsafeRunSync()

    assertEquals(parsed, expected)

    val length = Files.readAllLines(path).size()

    assert(length > 1)
  }

  test("raise an EmptyRefreshTokenFileException if the file is empty") {
    val path: Path = resourcePath("empty_refresh_token_file.txt")

    val program: IO[RefreshToken] = GoogleRefreshTokenParser.parse[IO](path)

    val caught: EmptyRefreshTokenFileException =
      intercept[EmptyRefreshTokenFileException] {
        program.unsafeRunSync()
      }

    assert(caught match {
      case EmptyRefreshTokenFileException(p) if p == path => true
      case _ => false
    })
  }

}
