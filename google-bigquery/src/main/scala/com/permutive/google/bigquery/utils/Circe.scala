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

package com.permutive.google.bigquery.utils

import io.circe.{Encoder, Printer}
import org.http4s.EntityEncoder
import org.http4s.circe.CirceInstances

private[bigquery] object Circe {

  // Have to manually specify this to ensure that nulls are removed
  val printerDropNullValues: Printer =
    Printer.noSpaces.copy(dropNullValues = true)

  private[this] val circeInstance: CirceInstances =
    CirceInstances.withPrinter(printerDropNullValues).build

  def circeEntityEncoderDropNullValues[F[_], T: Encoder]: EntityEncoder[F, T] =
    circeInstance.jsonEncoderOf[F, T]

}
