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

import com.permutive.google.auth.oauth.models.ServiceAccountAccessToken

import scala.concurrent.duration.FiniteDuration

trait ServiceAccountOAuth[F[_]] {

  /** Based on https://developers.google.com/identity/protocols/OAuth2ServiceAccount
    * @param iss
    *   The email address of the service account.
    * @param scope
    *   A space-delimited list of the permissions that the application requests.
    * @param exp
    *   The expiration time of the assertion, specified as milliseconds since 00:00:00 UTC, January 1, 1970.
    * @param iat
    *   The time the assertion was issued, specified as milliseconds since 00:00:00 UTC, January 1, 1970.
    */
  def authenticate(
      iss: String,
      scope: String,
      exp: Instant,
      iat: Instant
  ): F[Option[ServiceAccountAccessToken]]

  def maxDuration: FiniteDuration

}

object ServiceAccountOAuth {
  def apply[F[_]: ServiceAccountOAuth]: ServiceAccountOAuth[F] = implicitly
}
