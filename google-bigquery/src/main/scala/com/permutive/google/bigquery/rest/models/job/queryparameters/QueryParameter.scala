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

package com.permutive.google.bigquery.rest.models.job.queryparameters

import com.permutive.google.bigquery.models.SQLType
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import cats.syntax.functor._

import scala.collection.immutable.ListMap

/** Represents a BigQuery query parameter.
  *
  * This class does not necessarily need to be constructed directly: use [[QueryParameterEncoder]] to derive encoders to
  * a [[QueryParameter]] from a generic type.
  */
sealed abstract class QueryParameter(
    val name: Option[String],
    val parameterType: QueryParameterType,
    val parameterValue: QueryParameterValue
)

object QueryParameter {

  final private case class QueryParameterCC private (
      override val name: Option[String],
      override val parameterType: QueryParameterType,
      override val parameterValue: QueryParameterValue
  ) extends QueryParameter(name, parameterType, parameterValue)

  def apply(
      name: Option[String],
      parameterType: QueryParameterType,
      parameterValue: QueryParameterValue
  ): QueryParameter = new QueryParameter(name, parameterType, parameterValue) {}

  /** Used to construct a singular [[QueryParameter]] without a [[QueryParameterEncoder]].
    */
  def scalar(name: String, `type`: SQLType, value: String): QueryParameter =
    QueryParameter(
      Some(name),
      QueryParameterType.singular(`type`),
      QueryParameterValue.singular(value)
    )

  implicit val encoder: Encoder[QueryParameter] =
    deriveEncoder[QueryParameterCC].contramap[QueryParameter](qp =>
      QueryParameterCC(qp.name, qp.parameterType, qp.parameterValue)
    )
  implicit val decoder: Decoder[QueryParameter] = deriveDecoder[QueryParameterCC].widen
}

sealed abstract class QueryParameterValue(
    val value: Option[String],
    val arrayValues: Option[List[QueryParameterValue]],
    val structValues: Option[ListMapLike[String, QueryParameterValue]]
) {
  private[queryparameters] def setStructValues(
      values: Option[ListMapLike[String, QueryParameterValue]]
  ): QueryParameterValue =
    new QueryParameterValue(value, arrayValues, values) {}
}

object QueryParameterValue {

  case class QueryParameterValueCC private (
      override val value: Option[String],
      override val arrayValues: Option[List[QueryParameterValue]],
      override val structValues: Option[ListMapLike[String, QueryParameterValue]]
  ) extends QueryParameterValue(value, arrayValues, structValues)

  def apply(
      value: Option[String],
      arrayValues: Option[List[QueryParameterValue]],
      structValues: Option[ListMapLike[String, QueryParameterValue]]
  ): QueryParameterValue = new QueryParameterValue(value, arrayValues, structValues) {}

  def singular(s: String): QueryParameterValue =
    QueryParameterValue(
      value = Some(s),
      arrayValues = None,
      structValues = None
    )

  implicit val encoder: Encoder[QueryParameterValue] =
    deriveEncoder[QueryParameterValueCC].contramap[QueryParameterValue](qpv =>
      QueryParameterValueCC(qpv.value, qpv.arrayValues, qpv.structValues)
    )
  implicit val decoder: Decoder[QueryParameterValue] = deriveDecoder[QueryParameterValueCC].widen
}

sealed abstract class QueryParameterType(
    val `type`: SQLType,
    val arrayType: Option[QueryParameterType],
    val structTypes: Option[List[StructType]]
) {
  private[queryparameters] def setStructTypes(
      values: Option[List[StructType]]
  ): QueryParameterType =
    new QueryParameterType(`type`, arrayType, values) {}
}

object QueryParameterType {

  sealed private case class QueryParameterTypeCC private (
      override val `type`: SQLType,
      override val arrayType: Option[QueryParameterType],
      override val structTypes: Option[List[StructType]]
  ) extends QueryParameterType(`type`, arrayType, structTypes)

  def apply(
      `type`: SQLType,
      arrayType: Option[QueryParameterType],
      structTypes: Option[List[StructType]]
  ): QueryParameterType = new QueryParameterType(
    `type`,
    arrayType,
    structTypes
  ) {}

  def singular(sqlType: SQLType): QueryParameterType =
    QueryParameterType(`type` = sqlType, arrayType = None, structTypes = None)

  implicit val encoder: Encoder[QueryParameterType] =
    deriveEncoder[QueryParameterTypeCC].contramap[QueryParameterType](qpt =>
      QueryParameterTypeCC(qpt.`type`, qpt.arrayType, qpt.structTypes)
    )
  implicit val decoder: Decoder[QueryParameterType] = deriveDecoder[QueryParameterTypeCC].widen
}

sealed abstract class StructType(val name: Option[String], val `type`: QueryParameterType)

object StructType {
  final private case class StructTypeCC private (
      override val name: Option[String],
      override val `type`: QueryParameterType
  ) extends StructType(name, `type`)

  def apply(name: Option[String], `type`: QueryParameterType): StructType = new StructType(name, `type`) {}

  implicit val encoder: Encoder[StructType] =
    deriveEncoder[StructTypeCC].contramap[StructType](st => StructTypeCC(st.name, st.`type`))
  implicit val decoder: Decoder[StructType] = deriveDecoder[StructTypeCC].widen
}

sealed abstract class ListMapLike[A, B] private (val keyValues: List[(A, B)])

object ListMapLike {

  def apply[A, B](keyValues: List[(A, B)]): ListMapLike[A, B] = new ListMapLike(keyValues) {}

  implicit def encoder[A: Encoder]: Encoder[ListMapLike[String, A]] =
    Encoder[ListMap[String, A]].contramap[ListMapLike[String, A]] { lm =>
      ListMap(lm.keyValues: _*)
    }

  implicit def decoder[A: Decoder]: Decoder[ListMapLike[String, A]] =
    Decoder[ListMap[String, A]].map(m => ListMapLike(m.toList))

}
