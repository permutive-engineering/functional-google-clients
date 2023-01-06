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

package com.permutive.google.bigquery.rest.models.job

import scala.concurrent.duration._

/** Settings to control polling BigQuery for job completion.
  *
  * @param delay
  *   Delay between each poll to see if a job has completed
  * @param timeout
  *   How long to wait for a job to complete before raising a
  *   [[com.permutive.google.bigquery.rest.models.Exceptions.TimeoutException]]
  */
sealed abstract class PollSettings private (
    val delay: FiniteDuration,
    val timeout: FiniteDuration
) {

  private def copy(delay: FiniteDuration = delay, timeout: FiniteDuration = timeout) =
    new PollSettings(delay, timeout) {}

  def withDelay(delay: FiniteDuration): PollSettings =
    copy(delay = delay)

  def withTimeout(timeout: FiniteDuration): PollSettings =
    copy(timeout = timeout)

}

object PollSettings {
  def apply(delay: FiniteDuration, timeout: FiniteDuration): PollSettings = new PollSettings(delay, timeout) {}

  /** Default value for [[PollSettings]]. 500ms poll delay and 5 minute timeout.
    */
  val default: PollSettings =
    PollSettings(
      delay = 500.millis,
      timeout = 5.minutes
    )

}
