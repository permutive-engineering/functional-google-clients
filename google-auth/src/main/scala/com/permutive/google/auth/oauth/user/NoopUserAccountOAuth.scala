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

import cats.Applicative
import com.permutive.google.auth.oauth.models.AccessToken._
import com.permutive.google.auth.oauth.models.UserAccountAccessToken
import com.permutive.google.auth.oauth.user.models.NewTypes._

class NoopUserAccountOAuth[F[_]](implicit F: Applicative[F]) extends UserAccountOAuth[F] {

  override def authenticate(
      clientId: ClientId,
      clientSecret: ClientSecret,
      refreshToken: RefreshToken
  ): F[Option[UserAccountAccessToken]] =
    F.pure(
      Some(
        UserAccountAccessToken(
          Token("noop"),
          TokenType("noop"),
          ExpiresIn(3600)
        )
      )
    )

}
