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
import com.permutive.google.auth.oauth.user.models.GoogleUserAccount
import com.permutive.google.auth.oauth.user.models.NewTypes._
import io.circe.Decoder
import io.circe.parser._

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
    implicit final val decoder: Decoder[JsonGoogleInstalledSecrets] =
      Decoder.instance { hc =>
        val cursor = hc.downField("installed")

        for {
          clientId <- cursor.get[ClientId]("client_id")
          projectId <- cursor.get[String]("project_id")
          authUri <- cursor.get[String]("auth_uri")
          tokenUri <- cursor.get[String]("token_uri")
          authProviderX509CertUrl <- cursor.get[String](
            "auth_provider_x509_cert_url"
          )
          clientSecret <- cursor.get[ClientSecret]("client_secret")
          redirectUris <- cursor.get[List[String]]("redirect_uris")
        } yield JsonGoogleInstalledSecrets(
          JsonGoogleClientSecrets(
            clientId,
            projectId,
            authUri,
            tokenUri,
            authProviderX509CertUrl,
            clientSecret,
            redirectUris
          )
        )
      }
  }

  final def parse[F[_]](
      path: Path
  )(implicit F: Sync[F]): F[GoogleUserAccount] =
    F.blocking(Files.readString(path))
      .flatMap(decode[JsonGoogleInstalledSecrets](_).liftTo[F])
      .map { secrets =>
        GoogleUserAccount(
          secrets.installed.clientId,
          secrets.installed.clientSecret
        )
      }

}
