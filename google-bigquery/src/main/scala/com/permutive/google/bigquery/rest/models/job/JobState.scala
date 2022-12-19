package com.permutive.google.bigquery.rest.models.job

import enumeratum.EnumEntry.Uppercase
import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed trait JobState extends EnumEntry with Uppercase
object JobState extends Enum[JobState] with CirceEnum[JobState] {
  override val values = findValues

  case object Pending extends JobState
  case object Running extends JobState
  case object Done    extends JobState
}
