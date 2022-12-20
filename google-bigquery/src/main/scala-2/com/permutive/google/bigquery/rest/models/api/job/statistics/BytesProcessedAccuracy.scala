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

package com.permutive.google.bigquery.rest.models.api.job.statistics

import enumeratum.{CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.UpperSnakecase

import scala.collection.immutable

sealed trait BytesProcessedAccuracy extends EnumEntry with UpperSnakecase

object BytesProcessedAccuracy extends Enum[BytesProcessedAccuracy] with CirceEnum[BytesProcessedAccuracy] {
  override val values: immutable.IndexedSeq[BytesProcessedAccuracy] = findValues

  case object Precise extends BytesProcessedAccuracy
  case object LowerBound extends BytesProcessedAccuracy
  case object UpperBound extends BytesProcessedAccuracy
  case object Unknown extends BytesProcessedAccuracy
}
