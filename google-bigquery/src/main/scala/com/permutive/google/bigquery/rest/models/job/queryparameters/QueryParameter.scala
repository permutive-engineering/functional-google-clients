package com.permutive.google.bigquery.rest.models.job.queryparameters

import com.permutive.google.bigquery.models.SQLType
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import scala.collection.immutable.ListMap

/**
  * Represents a BigQuery query parameter.
  *
  * This class does not necessarily need to be constructed directly: use [[QueryParameterEncoder]] to derive encoders
  * to a [[QueryParameter]] from a generic type.
  */
case class QueryParameter(name: Option[String], parameterType: QueryParameterType, parameterValue: QueryParameterValue)

object QueryParameter {

  /** Used to construct a singular [[QueryParameter]] without a [[QueryParameterEncoder]]. */
  def scalar(name: String, `type`: SQLType, value: String): QueryParameter =
    QueryParameter(
      Some(name),
      QueryParameterType.singular(`type`),
      QueryParameterValue.singular(value),
    )

  implicit val encoder: Encoder.AsObject[QueryParameter] = deriveEncoder
  implicit val decoder: Decoder[QueryParameter]          = deriveDecoder
}

case class QueryParameterValue(
  value: Option[String],
  arrayValues: Option[List[QueryParameterValue]],
  structValues: Option[ListMapLike[String, QueryParameterValue]],
)

object QueryParameterValue {

  def singular(s: String): QueryParameterValue =
    QueryParameterValue(value = Some(s), arrayValues = None, structValues = None)

  implicit val encoder: Encoder.AsObject[QueryParameterValue] = deriveEncoder
  implicit val decoder: Decoder[QueryParameterValue]          = deriveDecoder
}

case class QueryParameterType(
  `type`: SQLType,
  arrayType: Option[QueryParameterType],
  structTypes: Option[List[StructType]],
)

object QueryParameterType {

  def singular(sqlType: SQLType): QueryParameterType =
    QueryParameterType(`type` = sqlType, arrayType = None, structTypes = None)

  implicit val encoder: Encoder.AsObject[QueryParameterType] = deriveEncoder
  implicit val decoder: Decoder[QueryParameterType]          = deriveDecoder
}

case class StructType(name: Option[String], `type`: QueryParameterType)

object StructType {
  implicit val encoder: Encoder.AsObject[StructType] = deriveEncoder
  implicit val decoder: Decoder[StructType]          = deriveDecoder
}

case class ListMapLike[A, B](keyValues: List[(A, B)])

object ListMapLike {

  implicit def encoder[A: Encoder]: Encoder[ListMapLike[String, A]] =
    Encoder[ListMap[String, A]].contramap { case ListMapLike(values) =>
      ListMap(values: _*)
    }

  implicit def decoder[A: Decoder]: Decoder[ListMapLike[String, A]] =
    Decoder[ListMap[String, A]].map(m => ListMapLike(m.toList))

}
