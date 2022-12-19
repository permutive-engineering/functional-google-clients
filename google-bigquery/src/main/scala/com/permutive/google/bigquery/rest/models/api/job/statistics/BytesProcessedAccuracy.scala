package com.permutive.google.bigquery.rest.models.api.job.statistics

import enumeratum.{CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.UpperSnakecase

import scala.collection.immutable

sealed trait BytesProcessedAccuracy extends EnumEntry with UpperSnakecase

object BytesProcessedAccuracy extends Enum[BytesProcessedAccuracy] with CirceEnum[BytesProcessedAccuracy] {
  override val values: immutable.IndexedSeq[BytesProcessedAccuracy] = findValues

  case object Precise    extends BytesProcessedAccuracy
  case object LowerBound extends BytesProcessedAccuracy
  case object UpperBound extends BytesProcessedAccuracy
  case object Unknown    extends BytesProcessedAccuracy
}
