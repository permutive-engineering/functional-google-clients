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

package com.permutive.google.auth.oauth.service

import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.time.Instant
import java.util.Date

import cats.effect.Sync
import cats.effect.kernel.Async
import cats.syntax.all._
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.permutive.google.auth.oauth.Constants
import com.permutive.google.auth.oauth.models.ServiceAccountAccessToken
import com.permutive.google.auth.oauth.models.api.AccessTokenApi
import com.permutive.google.auth.oauth.utils.HttpUtils
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.http4s.Method.POST
import org.http4s._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

import scala.concurrent.duration._

class GoogleServiceAccountOAuth[F[_]: Logger](
    key: RSAPrivateKey,
    googleOAuthRequestUri: Uri,
    httpClient: Client[F]
)(implicit
    F: Async[F]
) extends ServiceAccountOAuth[F]
    with Http4sClientDsl[F] {

  final private[this] val algorithm = Algorithm.RSA256(null: RSAPublicKey, key)

  final private[this] val description = "service account JWT"

  final private[this] val apiToServiceToken
      : AccessTokenApi => ServiceAccountAccessToken =
    token =>
      ServiceAccountAccessToken(
        token.accessToken,
        token.tokenType,
        token.expiresIn
      )

  final override def authenticate(
      iss: String,
      scope: String,
      exp: Instant,
      iat: Instant
  ): F[Option[ServiceAccountAccessToken]] = {
    val tokenF = F.delay(
      JWT.create
        .withIssuedAt(Date.from(iat))
        .withExpiresAt(Date.from(exp))
        .withAudience(googleOAuthRequestUri.toString)
        .withClaim("scope", scope)
        .withClaim("iss", iss)
        .sign(algorithm)
    )

    for {
      token <- tokenF
      form = UrlForm(
        "grant_type" -> "urn:ietf:params:oauth:grant-type:jwt-bearer",
        "assertion" -> token
      )
      res <- HttpUtils
        .fetchAccessTokenApi(
          httpClient,
          POST(form, googleOAuthRequestUri),
          description
        )

    } yield res.map(apiToServiceToken)
  }

  final override val maxDuration: FiniteDuration = 1.hour
}

object GoogleServiceAccountOAuth {

  def create[F[_]: Async](
      key: RSAPrivateKey,
      httpClient: Client[F]
  ): F[ServiceAccountOAuth[F]] =
    Slf4jLogger.create[F].flatMap { implicit logger =>
      Sync[F].delay(
        new GoogleServiceAccountOAuth(
          key,
          Constants.googleOAuthRequestUri,
          httpClient
        )
      )
    }

}
