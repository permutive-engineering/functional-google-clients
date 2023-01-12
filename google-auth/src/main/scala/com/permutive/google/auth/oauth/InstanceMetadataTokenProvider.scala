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

import cats.MonadThrow
import cats.effect.kernel.Async
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.permutive.google.auth.oauth.ServiceAccountTokenProvider.FailedToGetServiceToken
import com.permutive.google.auth.oauth.metadata.{
  GoogleInstanceMetadataOAuth,
  InstanceMetadataOAuth,
  NoopInstanceMetadataOAuth
}
import com.permutive.google.auth.oauth.models.ServiceAccountAccessToken
import org.http4s.client.Client

final class InstanceMetadataTokenProvider[F[_]: MonadThrow] private (
    auth: InstanceMetadataOAuth[F]
) extends TokenProvider[F, ServiceAccountAccessToken] {
  override val accessToken: F[ServiceAccountAccessToken] =
    auth.authenticate.flatMap(
      _.fold(FailedToGetServiceToken.raiseError[F, ServiceAccountAccessToken])(
        _.pure[F]
      )
    )
}

object InstanceMetadataTokenProvider {
  def apply[F[_]: InstanceMetadataTokenProvider]: InstanceMetadataTokenProvider[F] = implicitly

  def google[F[_]: Async](
      httpClient: Client[F]
  ): F[InstanceMetadataTokenProvider[F]] =
    GoogleInstanceMetadataOAuth
      .create[F](httpClient)
      .map(new InstanceMetadataTokenProvider[F](_))

  def noAuth[F[_]: MonadThrow] =
    new InstanceMetadataTokenProvider[F](new NoopInstanceMetadataOAuth[F])
}
