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

import java.util.concurrent.TimeUnit

import cats.effect.kernel.{Resource, Temporal}
import com.permutive.google.auth.oauth.models.AccessToken
import com.permutive.refreshable.Refreshable
import retry.{RetryDetails, RetryPolicy}

import scala.concurrent.duration._

object CachedTokenProvider {

  /** Suitable safety period for an token from the instance metadata, [[InstanceMetadataTokenProvider]].
    *
    * The GCP metadata endpoint caches tokens for 5 minutes until their expiry. The value here (4 minutes) should ensure
    * a new token will be provided and have no risk of requests using an expired token.
    *
    * See: https://cloud.google.com/compute/docs/access/create-enable-service-accounts-for-instances#applications
    */
  val InstanceMetadataOAuthSafetyPeriod: FiniteDuration = 4.minutes

  /** Generate a cached token provider from an underlying provider.
    *
    * @param underlying
    *   the underlying token provider to use when a new token is required
    * @param safetyPeriod
    *   how much time less than the indicated expiry to cache a token for; this is to give a safety buffer to ensure an
    *   expired token is never used in a request
    * @param onRefreshFailure
    *   what to do if retrying to refresh the token fails. The refresh fiber will have failed at this point and the
    *   token will grow stale. It is up to user handle this failure, as they see fit, in their application
    * @param onExhaustedRetries
    *   what to do if retrying to refresh the value fails. The refresh fiber will have failed at this point and the
    *   value will grow stale. It is up to user handle this failure, as they see fit, in their application
    * @param onNewToken
    *   a callback invoked whenever a new token is generated, the [[scala.concurrent.duration.FiniteDuration]] is the
    *   period that will be waited before the next new token
    * @param retryPolicy
    *   an optional configuration object for attempting to retry the effect of `fa` on failure. When no value is
    *   supplied this defaults to 5 retries with a delay between each of 200 milliseconds.
    */
  def resource[F[_]: Temporal, Token <: AccessToken](
      underlying: TokenProvider[F, Token],
      safetyPeriod: FiniteDuration,
      onRefreshFailure: (Throwable, RetryDetails) => F[Unit],
      onExhaustedRetries: PartialFunction[Throwable, F[Unit]] = PartialFunction.empty,
      onNewToken: Option[(Token, FiniteDuration) => F[Unit]] = None,
      retryPolicy: Option[RetryPolicy[F]] = None
  ): Resource[F, TokenProvider[F, Token]] = {
    val cacheDuration: Token => FiniteDuration = token =>
      // GCP access token lifetimes are specified in seconds.
      // If this is a negative amount then the sleep in `RefCache` will be for no time, it will not error.
      FiniteDuration(
        token.expiresIn.value.toLong,
        TimeUnit.SECONDS
      ) - safetyPeriod

    val baseBuilder = Refreshable
      .builder[F, Token](underlying.accessToken)
      .cacheDuration(cacheDuration)
      .onRefreshFailure { case (th, details) => onRefreshFailure(th, details) }
      .onExhaustedRetries(onExhaustedRetries)

    val newTokenBuilder = onNewToken.fold(baseBuilder)(baseBuilder.onNewValue)

    retryPolicy
      .fold(newTokenBuilder)(newTokenBuilder.retryPolicy(_))
      .resource
      .map { refreshable =>
        new TokenProvider[F, Token] {
          override val accessToken: F[Token] = refreshable.value
        }
      }
  }

}
