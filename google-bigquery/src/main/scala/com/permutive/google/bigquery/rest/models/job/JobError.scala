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

import cats.Eq
import cats.data.NonEmptyList
import com.permutive.google.bigquery.rest.models.api.ErrorProtoApi

sealed abstract class JobError private (
    val reason: String,
    val location: Option[
      String
    ], // Not strongly typed as docs aren't clear if this the same type as the Location we use elsewhere
    val message: String
) {
  override def equals(obj: Any): Boolean = obj match {
    case f: JobError => Eq[JobError].eqv(this, f)
    case _ => false
  }
}

object JobError {
  def apply(
      reason: String,
      location: Option[String],
      message: String
  ): JobError = new JobError(reason, location, message) {}

  private[rest] def one(e: ErrorProtoApi): JobError =
    JobError(e.reason, e.location, e.message)

  private[rest] def many(
      e: Option[ErrorProtoApi],
      es: Option[List[ErrorProtoApi]]
  ): Option[NonEmptyList[JobError]] =
    e.map(many(_, es)).orElse(es.flatMap(many))

  private[rest] def many(
      e: ErrorProtoApi,
      es: Option[List[ErrorProtoApi]]
  ): NonEmptyList[JobError] =
    es match {
      case Some(esList) => many(esList).getOrElse(NonEmptyList.one(one(e)))
      case None => NonEmptyList.one(one(e))
    }

  private[rest] def many(
      es: List[ErrorProtoApi]
  ): Option[NonEmptyList[JobError]] =
    NonEmptyList.fromList(es.map(one))

  implicit val eq: Eq[JobError] = Eq.instance { (x, y) =>
    x.reason == y.reason && x.location == y.location && x.message == y.message
  }
}
