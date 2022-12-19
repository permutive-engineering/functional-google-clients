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
import enumeratum.EnumEntry.UpperSnakecase

// Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/jobs

sealed trait WriteDisposition extends EnumEntry with UpperSnakecase
object WriteDisposition
    extends Enum[WriteDisposition]
    with CirceEnum[WriteDisposition] {
  override val values = findValues

  case object WriteTruncate extends WriteDisposition
  case object WriteAppend extends WriteDisposition

  //    // Commented out as unsupported here (error handling needed) but is supported by Google
  //    case object WriteEmpty    extends WriteDisposition
}
