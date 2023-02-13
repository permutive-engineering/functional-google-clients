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

package com.permutive.google.bigquery.configuration

import java.util.concurrent.TimeoutException

import cats.Applicative
import com.permutive.google.bigquery.models.Exceptions.BigQueryException
import retry.{RetryDetails, RetryPolicy}

import scala.util.control.NonFatal

sealed abstract class RetryConfiguration[F[_]] private (
    val policy: RetryPolicy[F],
    val shouldRetry: Throwable => F[Boolean],
    val onError: (Throwable, RetryDetails) => F[Unit]
)

object RetryConfiguration {
  def retryNonFatalBigQuery[F[_]: Applicative](policy: RetryPolicy[F]): RetryConfiguration[F] = apply(
    policy,
    {
      case _: BigQueryException => false
      case NonFatal(_) => true
      case _ => false
    }
  )

  def retryTimeout[F[_]: Applicative](policy: RetryPolicy[F]): RetryConfiguration[F] = apply(
    policy,
    {
      case _: TimeoutException => true
      case _ => false
    }
  )

  def apply[F[_]: Applicative](policy: RetryPolicy[F], shouldRetry: Throwable => Boolean): RetryConfiguration[F] =
    apply[F](
      policy,
      (th: Throwable) => Applicative[F].pure(shouldRetry(th)),
      (_: Throwable, _: RetryDetails) => Applicative[F].unit
    )

  def apply[F[_]](
      policy: RetryPolicy[F],
      shouldRetry: Throwable => F[Boolean],
      onError: (Throwable, RetryDetails) => F[Unit]
  ): RetryConfiguration[F] =
    new RetryConfiguration[F](policy, shouldRetry, onError) {}
}
