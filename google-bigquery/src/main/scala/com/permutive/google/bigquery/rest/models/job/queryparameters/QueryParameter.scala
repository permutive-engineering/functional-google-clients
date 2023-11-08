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

import cats.kernel.Eq
import com.permutive.google.bigquery.models.SQLType
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._

import scala.annotation.nowarn
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
) {
  override def equals(obj: Any): Boolean = obj match {
    case f: QueryParameter => Eq[QueryParameter].eqv(this, f)
    case _ => false
  }
}

object QueryParameter {
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

  implicit val eq: Eq[QueryParameter] = Eq.instance { (x, y) =>
    x.name == y.name && Eq[QueryParameterType].eqv(x.parameterType, y.parameterType) && Eq[QueryParameterValue].eqv(
      x.parameterValue,
      y.parameterValue
    )
  }

  implicit val encoder: Encoder[QueryParameter] =
    Encoder.forProduct3("name", "parameterType", "parameterValue")(x => (x.name, x.parameterType, x.parameterValue))
  implicit val decoder: Decoder[QueryParameter] = Decoder.forProduct3("name", "parameterType", "parameterValue")(apply)
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

  override def equals(obj: Any): Boolean = obj match {
    case f: QueryParameterValue => Eq[QueryParameterValue].eqv(this, f)
    case _ => false
  }
}

object QueryParameterValue {
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

  implicit val eq: Eq[QueryParameterValue] = Eq.instance { (x, y) =>
    x.value == y.value && x.arrayValues == y.arrayValues && x.structValues == y.structValues
  }

  implicit val encoder: Encoder[QueryParameterValue] =
    Encoder.instance(x =>
      Json.obj(
        "value" := x.value,
        "arrayValues" := x.arrayValues,
        "structValues" := x.structValues
      )
    )

  implicit val decoder: Decoder[QueryParameterValue] =
    Decoder.instance(c =>
      for {
        value <- c.get[Option[String]]("value")
        arrayValues <- c.get[Option[List[QueryParameterValue]]]("arrayValues")
        structValues <- c.get[Option[ListMapLike[String, QueryParameterValue]]]("structValues")
      } yield apply(value, arrayValues, structValues)
    )
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

  override def equals(obj: Any): Boolean = obj match {
    case f: QueryParameterType => Eq[QueryParameterType].eqv(this, f)
    case _ => false
  }
}

object QueryParameterType {
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

  implicit val eq: Eq[QueryParameterType] = Eq.instance { (x, y) =>
    x.`type` == y.`type` && x.arrayType == y.arrayType && Eq[Option[List[StructType]]].eqv(x.structTypes, y.structTypes)
  }

  implicit val encoder: Encoder[QueryParameterType] =
    Encoder.instance(x =>
      Json.obj(
        "type" := x.`type`,
        "arrayType" := x.arrayType,
        "structTypes" := x.structTypes
      )
    )
  implicit val decoder: Decoder[QueryParameterType] =
    Decoder.instance(c =>
      for {
        typ <- c.get[SQLType]("type")
        arrayType <- c.get[Option[QueryParameterType]]("arrayType")
        structTypes <- c.get[Option[List[StructType]]]("structTypes")
      } yield apply(typ, arrayType, structTypes)
    )
}

sealed abstract class StructType(val name: Option[String], val `type`: QueryParameterType) {
  override def equals(obj: Any): Boolean = obj match {
    case f: StructType => Eq[StructType].eqv(this, f)
    case _ => false
  }
}

object StructType {
  def apply(name: Option[String], `type`: QueryParameterType): StructType = new StructType(name, `type`) {}

  implicit val eq: Eq[StructType] = Eq.instance { (x, y) =>
    x.name == y.name && x.`type` == y.`type`
  }

  implicit val encoder: Encoder[StructType] =
    Encoder.forProduct2("name", "type")(x => (x.name, x.`type`))
  implicit val decoder: Decoder[StructType] = Decoder.forProduct2("name", "type")(apply)
}

sealed abstract class ListMapLike[A, B] private (val keyValues: List[(A, B)]) {
  @nowarn
  override def equals(obj: Any): Boolean = obj match {
    case l: ListMapLike[A, B] => Eq[ListMapLike[A, B]].eqv(this, l)
    case _ => false
  }
}

object ListMapLike extends ListMapLikeLowPriority0 {

  def apply[A, B](keyValues: List[(A, B)]): ListMapLike[A, B] = new ListMapLike(keyValues) {}

  implicit def eq[A: Eq, B: Eq]: Eq[ListMapLike[A, B]] = Eq.by(_.keyValues)

  implicit def encoder[A: Encoder]: Encoder[ListMapLike[String, A]] =
    Encoder[ListMap[String, A]].contramap[ListMapLike[String, A]] { lm =>
      ListMap(lm.keyValues: _*)
    }

  implicit def decoder[A: Decoder]: Decoder[ListMapLike[String, A]] =
    Decoder[ListMap[String, A]].map(m => ListMapLike(m.toList))
}

trait ListMapLikeLowPriority0 {
  implicit def eq0[A, B]: Eq[ListMapLike[A, B]] = Eq.instance { (x, y) =>
    x.keyValues == y.keyValues
  }
}
