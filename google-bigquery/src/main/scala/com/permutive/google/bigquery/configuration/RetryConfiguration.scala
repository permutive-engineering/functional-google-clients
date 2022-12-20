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

import com.permutive.google.bigquery.models.Exceptions.BigQueryException

import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

/** Configuration to control retrying requests to BigQuery.
  *
  * Some requests may be observed to time-out for example. BigQuery is frequently idempotent (e.g. creating a job when a
  * job ID is specified), so naive retries may be used to alleviate this.
  *
  * Enclosing case case class for [[fs2.Stream.retry]] parameters.
  *
  * @param delay
  *   Duration of delay before the first retry
  * @param nextDelay
  *   Applied to the previous delay to compute the next, e.g. to implement exponential backoff
  * @param maxAttempts
  *   Number of attempts before failing with the latest error
  * @param retriable
  *   Function to determine whether a failure is retriable or not. A failure is immediately returned when a
  *   non-retriable failure is encountered. Defaults to retry every `NonFatal` exception not raised by this library.
  */
case class RetryConfiguration(
    delay: FiniteDuration,
    nextDelay: FiniteDuration => FiniteDuration,
    maxAttempts: Int,
    retriable: Throwable => Boolean = RetryConfiguration.retryNonFatalNonBigQuery
)

object RetryConfiguration {

  val retryNonFatalNonBigQuery: Throwable => Boolean = {
    case _: BigQueryException => false
    case NonFatal(_) => true
    case _ => false
  }

  val retryNothing: Throwable => Boolean =
    _ => false

  /** TimeoutException raised by Blaze client in case of timeout. */
  val retryTimeout: Throwable => Boolean = {
    case _: TimeoutException => true
    case _ => false
  }

  /** A [[RetryConfiguration]] which always delays by the same amount.
    *
    * @param delay
    *   Duration of delay between retries
    * @param maxAttempts
    *   Number of attempts before failing with the latest error
    * @param retriable
    *   Function to determine whether a failure is retriable or not. A failure is immediately returned when a
    *   non-retriable failure is encountered. Defaults to retry every `NonFatal` exception not raised by this library.
    */
  def fixedDelay(
      delay: FiniteDuration,
      maxAttempts: Int,
      retriable: Throwable => Boolean = retryNonFatalNonBigQuery
  ): RetryConfiguration =
    RetryConfiguration(delay, identity, maxAttempts, retriable)

  /** A [[RetryConfiguration]] which geometrically increases delay to a maximum.
    *
    * @param initialDelay
    *   Duration of delay before the first retry
    * @param nextDelayMultiple
    *   The ratio of one delay to the next.
    * @param nextDelayMaximum
    *   The maximum delay that should ever occur between retries.
    * @param maxAttempts
    *   Number of attempts before failing with the latest error
    * @param retriable
    *   Function to determine whether a failure is retriable or not. A failure is immediately returned when a
    *   non-retriable failure is encountered. Defaults to retry every `NonFatal` exception not raised by this library.
    */
  def geometricBackoff(
      initialDelay: FiniteDuration,
      nextDelayMultiple: Long,
      nextDelayMaximum: FiniteDuration,
      maxAttempts: Int,
      retriable: Throwable => Boolean = retryNonFatalNonBigQuery
  ): RetryConfiguration =
    RetryConfiguration(
      initialDelay,
      current => (current * nextDelayMultiple).min(nextDelayMaximum),
      maxAttempts,
      retriable
    )

}
