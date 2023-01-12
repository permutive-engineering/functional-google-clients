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

import java.nio.file.{Path, Paths}

import cats.effect.IO
import com.permutive.google.auth.oauth.user.crypto.GoogleClientSecretsParser.GoogleUserAccount
import com.permutive.google.auth.oauth.user.models.NewTypes.{ClientId, ClientSecret}
import munit.CatsEffectSuite

class GoogleClientSecretsParserSpec extends CatsEffectSuite {

  private def resourcePath(fileName: String): Path =
    Paths.get(getClass.getResource(fileName).getFile)

  test("parse a valid file") {
    val path: Path = resourcePath("valid_client_secrets_file.json")
    val expected: GoogleUserAccount =
      GoogleUserAccount(
        ClientId("my-client-id"),
        ClientSecret("my-client-secret")
      )

    val parsed = GoogleClientSecretsParser.parse[IO](path).unsafeRunSync()

    assertEquals(parsed, expected)
  }

}
