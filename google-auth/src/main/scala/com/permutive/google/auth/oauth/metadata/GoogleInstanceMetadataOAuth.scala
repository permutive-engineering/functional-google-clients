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

package com.permutive.google.auth.oauth.metadata

import cats.effect.kernel.Async
import cats.syntax.functor._
import com.permutive.google.auth.oauth.Constants
import com.permutive.google.auth.oauth.models.ServiceAccountAccessToken
import com.permutive.google.auth.oauth.models.api.AccessTokenApi
import com.permutive.google.auth.oauth.utils.HttpUtils
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.{Header, Uri}
import org.typelevel.ci.CIString
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

// Obtains OAuth token from instance metadata
// https://cloud.google.com/compute/docs/access/create-enable-service-accounts-for-instances#applications
class GoogleInstanceMetadataOAuth[F[_]: Async: Logger](
    googleInstanceMetadataTokenUri: Uri,
    httpClient: Client[F]
) extends InstanceMetadataOAuth[F]
    with Http4sClientDsl[F] {

  final private[this] val description = "instance metadata endpoint"

  final private[this] val apiToServiceToken
      : AccessTokenApi => ServiceAccountAccessToken =
    token =>
      ServiceAccountAccessToken(
        token.accessToken,
        token.tokenType,
        token.expiresIn
      )

  override def authenticate: F[Option[ServiceAccountAccessToken]] =
    HttpUtils
      .fetchAccessTokenApi(
        httpClient,
        GET(
          googleInstanceMetadataTokenUri,
          Header.Raw(CIString("Metadata-Flavor"), "Google")
        ),
        description
      )
      .map(_.map(apiToServiceToken))
}

object GoogleInstanceMetadataOAuth {
  def create[F[_]: Async](
      httpClient: Client[F]
  ): F[GoogleInstanceMetadataOAuth[F]] =
    Slf4jLogger.create[F].map { implicit lg =>
      new GoogleInstanceMetadataOAuth[F](
        Constants.googleInstanceMetadataTokenUri,
        httpClient
      )
    }
}
