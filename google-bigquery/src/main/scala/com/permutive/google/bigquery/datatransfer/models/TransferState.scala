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

package com.permutive.google.bigquery.datatransfer.models

import enumeratum.EnumEntry.UpperSnakecase
import enumeratum.{CirceEnum, Enum, EnumEntry}

import scala.collection.immutable

// Here as it may be exposed "publicly" at some point
// Documentation: https://cloud.google.com/bigquery/docs/reference/datatransfer/rest/v1/TransferState
sealed trait TransferState extends EnumEntry with UpperSnakecase
object TransferState extends Enum[TransferState] with CirceEnum[TransferState] {
  override val values: immutable.IndexedSeq[TransferState] = findValues

  case object TransferStateUnspecified extends TransferState
  case object Pending extends TransferState
  case object Running extends TransferState
  case object Succeeded extends TransferState
  case object Failed extends TransferState
  case object Cancelled extends TransferState
}
