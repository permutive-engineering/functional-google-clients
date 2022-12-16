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

import cats.effect.Sync
import cats.syntax.all._
import com.github.plokhotnyuk.jsoniter_scala.core.{
  readFromArray,
  JsonValueCodec
}
import com.github.plokhotnyuk.jsoniter_scala.macros.{
  CodecMakerConfig,
  JsonCodecMaker
}
import com.permutive.google.auth.oauth.user.models.GoogleUserAccount
import com.permutive.google.auth.oauth.user.models.NewTypes._

object GoogleClientSecretsParser {

  case class JsonGoogleInstalledSecrets(
      installed: JsonGoogleClientSecrets
  )

  case class JsonGoogleClientSecrets(
      clientId: ClientId,
      projectId: String,
      authUri: String,
      tokenUri: String,
      authProviderX509CertUrl: String,
      clientSecret: ClientSecret,
      redirectUris: List[String]
  )

  object JsonGoogleInstalledSecrets {
    implicit final val codec: JsonValueCodec[JsonGoogleInstalledSecrets] =
      JsonCodecMaker.make[JsonGoogleInstalledSecrets](
        CodecMakerConfig.withFieldNameMapper(JsonCodecMaker.enforce_snake_case)
      )
  }

  final def parse[F[_]](path: Path)(implicit F: Sync[F]): F[GoogleUserAccount] =
    F.delay(readFromArray[JsonGoogleInstalledSecrets](Files.readAllBytes(path)))
      .map { secrets =>
        GoogleUserAccount(
          secrets.installed.clientId,
          secrets.installed.clientSecret
        )
      }

}
