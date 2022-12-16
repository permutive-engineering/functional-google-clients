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

import cats.effect.MonadCancelThrow
import cats.effect.kernel.Resource
import cats.syntax.functor._
import com.permutive.google.auth.oauth.models.AccessToken
import org.http4s.Credentials
import org.http4s.client.Client
import org.http4s.headers.Authorization
import org.typelevel.ci.CIString

object AuthedClient {
  def apply[F[_]: MonadCancelThrow, Token <: AccessToken](
      client: Client[F],
      tokenProvider: TokenProvider[F, Token]
  ): Client[F] = Client[F] { request =>
    Resource
      .eval(tokenProvider.accessToken.map { token =>
        request.putHeaders(
          Authorization(
            Credentials
              .Token(CIString(token.tokenType.value), token.accessToken.value)
          )
        )
      })
      .flatMap(client.run)
  }
}
