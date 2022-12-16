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

import java.time.Instant

import cats.Applicative
import com.permutive.google.auth.oauth.models.AccessToken._
import com.permutive.google.auth.oauth.models.ServiceAccountAccessToken

import scala.concurrent.duration._

class NoopServiceAccountOAuth[F[_]](implicit F: Applicative[F])
    extends ServiceAccountOAuth[F] {

  final override def authenticate(
      iss: String,
      scope: String,
      exp: Instant,
      iat: Instant
  ): F[Option[ServiceAccountAccessToken]] =
    F.pure(
      Some(
        ServiceAccountAccessToken(
          Token("noop"),
          TokenType("noop"),
          ExpiresIn(3600)
        )
      )
    )

  final override val maxDuration: FiniteDuration = 1.hour

}
