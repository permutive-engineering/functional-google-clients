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

package com.permutive.google.auth.oauth

import java.io.File
import java.nio.file.Path

import cats.MonadError
import cats.effect.kernel.{Async, Sync}
import cats.syntax.all._
import com.permutive.google.auth.oauth.models.UserAccountAccessToken
import com.permutive.google.auth.oauth.user.crypto.{
  GoogleClientSecretsParser,
  GoogleRefreshTokenParser
}
import com.permutive.google.auth.oauth.user.models.NewTypes._
import com.permutive.google.auth.oauth.user.{
  GoogleUserAccountOAuth,
  NoopUserAccountOAuth,
  UserAccountOAuth
}
import com.permutive.google.auth.oauth.utils.ApplicationDefaultCredentials
import org.http4s.client.Client

class UserAccountTokenProvider[F[_]](
    clientId: ClientId,
    clientSecret: ClientSecret,
    refreshToken: RefreshToken,
    auth: UserAccountOAuth[F]
)(implicit
    ME: MonadError[F, Throwable]
) extends TokenProvider[F, UserAccountAccessToken] {

  import UserAccountTokenProvider._

  override val accessToken: F[UserAccountAccessToken] = {
    for {
      token <- auth.authenticate(clientId, clientSecret, refreshToken)
      tokenOrError <- token.fold(
        ME.raiseError[UserAccountAccessToken](FailedToGetUserToken)
      )(_.pure[F])
    } yield tokenOrError
  }

}

object UserAccountTokenProvider {

  def apply[F[_]: UserAccountTokenProvider]: UserAccountTokenProvider[F] =
    implicitly

  def google[F[_]](
      clientSecretsPath: String,
      refreshTokenPath: String,
      httpClient: Client[F]
  )(implicit
      F: Async[F]
  ): F[UserAccountTokenProvider[F]] =
    for {
      secretsFile <- path(clientSecretsPath)
      userAccount <- GoogleClientSecretsParser.parse(secretsFile)
      tokenFile <- path(refreshTokenPath)
      token <- GoogleRefreshTokenParser.parse(tokenFile)
      googleProv <- google(
        userAccount.clientId,
        userAccount.clientSecret,
        token,
        httpClient
      )
    } yield googleProv

  def google[F[_]: Async](
      clientId: ClientId,
      clientSecret: ClientSecret,
      refreshToken: RefreshToken,
      httpClient: Client[F]
  ): F[UserAccountTokenProvider[F]] =
    GoogleUserAccountOAuth
      .create(httpClient)
      .map(
        new UserAccountTokenProvider(clientId, clientSecret, refreshToken, _)
      )

  // load application default credentials from ~/.config/cloud/application_default_credentials.json
  // create this file with `gcloud auth application-default login`,
  // or set `GOOGLE_APPLICATION_CREDENTIALS` to override to another path
  def applicationDefault[F[_]: Async](
      httpClient: Client[F]
  ): F[UserAccountTokenProvider[F]] =
    ApplicationDefaultCredentials.read.flatMap { creds =>
      google(creds.clientId, creds.clientSecret, creds.refreshToken, httpClient)
    }

  @inline
  private def path[F[_]](path: String)(implicit F: Sync[F]): F[Path] =
    F.delay(new File(path).toPath)

  case object FailedToGetUserToken
      extends RuntimeException("Failed to get user token")

  def noAuth[F[_]](implicit
      ME: MonadError[F, Throwable]
  ): UserAccountTokenProvider[F] =
    new UserAccountTokenProvider[F](
      ClientId("noop"),
      ClientSecret("noop"),
      RefreshToken("noop"),
      new NoopUserAccountOAuth[F]
    )

}
