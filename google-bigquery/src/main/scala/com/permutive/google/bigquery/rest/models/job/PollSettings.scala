package com.permutive.google.bigquery.rest.models.job

import scala.concurrent.duration._

/**
  * Settings to control polling BigQuery for job completion.
  *
  * @param delay Delay between each poll to see if a job has completed
  * @param timeout How long to wait for a job to complete before raising a
  *                [[com.permutive.google.bigquery.rest.models.Exceptions.TimeoutException]]
  */
case class PollSettings(
  delay: FiniteDuration,
  timeout: FiniteDuration,
) {

  def withDelay(delay: FiniteDuration): PollSettings =
    copy(delay = delay)

  def withTimeout(timeout: FiniteDuration): PollSettings =
    copy(timeout = timeout)

}

object PollSettings {

  /**
    * Default value for [[PollSettings]]. 500ms poll delay and 5 minute timeout.
    */
  val default: PollSettings =
    PollSettings(
      delay = 500.millis,
      timeout = 5.minutes,
    )

}
