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

package com.permutive.google.auth.oauth.user

import cats.effect.Sync
import cats.effect.kernel.Async
import cats.syntax.all._
import com.permutive.google.auth.oauth.Constants
import com.permutive.google.auth.oauth.models.UserAccountAccessToken
import com.permutive.google.auth.oauth.models.api.AccessTokenApi
import com.permutive.google.auth.oauth.user.models.NewTypes._
import com.permutive.google.auth.oauth.utils.HttpUtils
import org.http4s.Method.POST
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{Uri, UrlForm}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class GoogleUserAccountOAuth[F[_]: Async: Logger](
    googleOAuthRequestUri: Uri,
    httpClient: Client[F]
) extends UserAccountOAuth[F]
    with Http4sClientDsl[F] {

  // Documentation: https://developers.google.com/identity/protocols/OAuth2WebServer#exchange-authorization-code

  final private[this] val description = "user account"

  final private[this] val apiToUserToken: AccessTokenApi => UserAccountAccessToken =
    token =>
      UserAccountAccessToken(
        token.accessToken,
        token.tokenType,
        token.expiresIn
      )

  override def authenticate(
      clientId: ClientId,
      clientSecret: ClientSecret,
      refreshToken: RefreshToken
  ): F[Option[UserAccountAccessToken]] = {
    val form = UrlForm(
      "refresh_token" -> refreshToken.value,
      "client_id" -> clientId.value,
      "client_secret" -> clientSecret.value,
      "grant_type" -> "refresh_token"
    )

    HttpUtils
      .fetchAccessTokenApi(
        httpClient,
        POST(form, googleOAuthRequestUri),
        description
      )
      .map(_.map(apiToUserToken))
  }

}

object GoogleUserAccountOAuth {

  def create[F[_]: Async](httpClient: Client[F]): F[UserAccountOAuth[F]] =
    Slf4jLogger.create[F].flatMap { implicit logger =>
      Sync[F].delay(
        new GoogleUserAccountOAuth(Constants.googleOAuthRequestUri, httpClient)
      )
    }
}
