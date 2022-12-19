package com.permutive.google.bigquery.rest.models.job

import cats.data.NonEmptyList
import com.permutive.google.bigquery.rest.models.api.ErrorProtoApi
import io.scalaland.chimney.dsl._

case class JobError(
  reason: String,
  location: Option[
    String
  ], // Not strongly typed as docs aren't clear if this the same type as the Location we use elsewhere
  message: String,
)

object JobError {

  private[rest] def one(e: ErrorProtoApi): JobError =
    e.transformInto[JobError]

  private[rest] def many(e: Option[ErrorProtoApi], es: Option[List[ErrorProtoApi]]): Option[NonEmptyList[JobError]] =
    e.map(many(_, es)).orElse(es.flatMap(many))

  private[rest] def many(e: ErrorProtoApi, es: Option[List[ErrorProtoApi]]): NonEmptyList[JobError] =
    es match {
      case Some(esList) => many(esList).getOrElse(NonEmptyList.one(one(e)))
      case None         => NonEmptyList.one(one(e))
    }

  private[rest] def many(es: List[ErrorProtoApi]): Option[NonEmptyList[JobError]] =
    NonEmptyList.fromList(es.transformInto[List[JobError]])

}
