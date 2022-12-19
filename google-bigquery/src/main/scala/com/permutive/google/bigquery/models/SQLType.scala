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

package com.permutive.google.bigquery.models

import enumeratum.{CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.Uppercase

sealed trait SQLType extends EnumEntry with Uppercase
object SQLType extends Enum[SQLType] with CirceEnum[SQLType] {
  override val values = findValues

  case object Bytes extends SQLType
  case object Boolean extends SQLType
  case object Date extends SQLType
  case object Time extends SQLType
  case object Datetime extends SQLType
  case object Timestamp extends SQLType
  case object Float extends SQLType
  case object Integer extends SQLType
  case object String extends SQLType
  case object Record extends SQLType
  case object Geography extends SQLType
  case object Numeric extends SQLType
  case object Array extends SQLType
  case object Struct extends SQLType
  case object Int64 extends SQLType
  case object Float64 extends SQLType

  // These may need all of the types from these locations ultimately:
  // https://github.com/googleapis/google-cloud-java/blob/master/google-cloud-clients/google-cloud-bigquery/src/main/java/com/google/cloud/bigquery/StandardSQLTypeName.java
  // https://github.com/googleapis/google-cloud-java/blob/master/google-cloud-clients/google-cloud-bigquery/src/main/java/com/google/cloud/bigquery/LegacySQLTypeName.java
}
