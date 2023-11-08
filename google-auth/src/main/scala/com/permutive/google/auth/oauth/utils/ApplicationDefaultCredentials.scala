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

package com.permutive.google.auth.oauth.utils

import java.io.File

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all._
import com.permutive.google.auth.oauth.user.models.NewTypes.{ClientId, ClientSecret, RefreshToken}
import fs2.io.file.{Files, Flags}
import io.circe.fs2._
import io.circe.{Decoder, HCursor}

object ApplicationDefaultCredentials {

  sealed abstract class Credentials private (
      val clientId: ClientId,
      val clientSecret: ClientSecret,
      val refreshToken: RefreshToken
  )

  object Credentials {
    private[utils] def apply(id: ClientId, secret: ClientSecret, token: RefreshToken): Credentials =
      new Credentials(id, secret, token) {}
  }

  implicit val decodeCredentials: Decoder[Credentials] = (c: HCursor) =>
    for {
      id <- c.downField("client_id").as[String]
      secret <- c.downField("client_secret").as[String]
      refresh <- c.downField("refresh_token").as[String]
    } yield Credentials(
      ClientId(id),
      ClientSecret(secret),
      RefreshToken(refresh)
    )

  // adapted from com.google.api.client.googleapis.auth.oauth2.DefaultCredentialProvider
  def read[F[_]: Async]: F[Credentials] =
    Sync[F].blocking(
      environmentOverrideCredentialsFile.getOrElse(defaultCredentialsFile)
    ) >>= { credentialsFile =>
      Files.forAsync[F]
        .readAll(
          fs2.io.file.Path.fromNioPath(credentialsFile.toPath),
          4096,
          Flags.Read
        )
        .through(byteStreamParser)
        .through(decoder[F, Credentials])
        .compile
        .lastOrError
    }

  private def environmentOverrideCredentialsFile: Option[File] =
    sys.env.get("GOOGLE_APPLICATION_CREDENTIALS").map(new File(_))

  private def defaultCredentialsFile: File = {
    val sdkConfigDirectory = "gcloud"
    val credentialsFileName = "application_default_credentials.json"
    val os = sys.props.getOrElse("os.name", "").toLowerCase()

    val configPath = if (os.indexOf("windows") >= 0) {
      val appDataPath = new File(sys.env("APPDATA"))
      new File(appDataPath, sdkConfigDirectory)
    } else {
      val configPath = new File(sys.props.getOrElse("user.home", ""), ".config")
      new File(configPath, sdkConfigDirectory)
    }

    new File(configPath, credentialsFileName)
  }
}
