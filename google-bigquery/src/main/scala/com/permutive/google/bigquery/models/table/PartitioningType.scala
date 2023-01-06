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

package com.permutive.google.bigquery.models.table

import enumeratum.EnumEntry.Uppercase
import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed trait PartitioningType extends EnumEntry with Uppercase
object PartitioningType extends Enum[PartitioningType] with CirceEnum[PartitioningType] {
  override val values = findValues

  case object Day extends PartitioningType
  case object Hour extends PartitioningType
  case object Month extends PartitioningType
  case object Year extends PartitioningType
}
