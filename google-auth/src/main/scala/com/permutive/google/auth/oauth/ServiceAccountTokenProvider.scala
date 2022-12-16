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
import java.security.interfaces.RSAPrivateKey
import java.time.Instant

import cats.effect.kernel.{Async, Sync}
import cats.syntax.all._
import com.permutive.google.auth.oauth.models._
import com.permutive.google.auth.oauth.service.crypto.GoogleServiceAccountParser
import com.permutive.google.auth.oauth.service.{
  GoogleServiceAccountOAuth,
  NoopServiceAccountOAuth,
  ServiceAccountOAuth
}
import org.http4s.client.Client

class ServiceAccountTokenProvider[F[_]](
    emailAddress: String,
    scope: List[String],
    auth: ServiceAccountOAuth[F]
)(implicit
    F: Sync[F]
) extends TokenProvider[F, ServiceAccountAccessToken] {

  import ServiceAccountTokenProvider._

  override val accessToken: F[ServiceAccountAccessToken] = {
    for {
      now <- F.delay(Instant.now())
      token <- auth.authenticate(
        emailAddress,
        // [Ben - 2019-01-26]
        // Google documentation states this should be comma delimited, but actually must be space delimited
        // Determined by:
        //   * Trial and error; and
        //   * Observing requests sent when using the API in a web browser
        scope.mkString(" "),
        now.plusMillis(auth.maxDuration.toMillis),
        now
      )
      tokenOrError <- token.fold(
        F.raiseError[ServiceAccountAccessToken](FailedToGetServiceToken)
      )(_.pure[F])
    } yield tokenOrError
  }

}

object ServiceAccountTokenProvider {

  def apply[F[_]: ServiceAccountTokenProvider]: ServiceAccountTokenProvider[F] =
    implicitly

  case object FailedToGetServiceToken
      extends RuntimeException("Failed to get service token")

  def google[F[_]](
      serviceAccountPath: String,
      scope: List[String],
      httpClient: Client[F]
  )(implicit
      F: Async[F]
  ): F[ServiceAccountTokenProvider[F]] =
    for {
      file <- F.delay(new File(serviceAccountPath).toPath)
      serviceAccount <- GoogleServiceAccountParser.parse(file)
      googleProv <- google(
        serviceAccount.clientEmail,
        serviceAccount.privateKey,
        scope,
        httpClient
      )
    } yield googleProv

  def google[F[_]: Async](
      clientEmail: String,
      privateKey: RSAPrivateKey,
      scope: List[String],
      httpClient: Client[F]
  ): F[ServiceAccountTokenProvider[F]] =
    GoogleServiceAccountOAuth
      .create(privateKey, httpClient)
      .map(new ServiceAccountTokenProvider(clientEmail, scope, _))

  def noAuth[F[_]: Sync]: ServiceAccountTokenProvider[F] =
    new ServiceAccountTokenProvider("noop", Nil, new NoopServiceAccountOAuth)

}
