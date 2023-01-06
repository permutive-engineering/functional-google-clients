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
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

trait PlatformSpecificParameterEncoders {

  /** Encode an empty HList as an empty struct parameter
    *
    * Used in derivation as the seed for encoding structs.
    */
  implicit val hNilParameterEncoder: ParameterEncoder[HNil] =
    new ParameterEncoder[HNil] {
      override def value(hnil: HNil): QueryParameterValue =
        QueryParameterValue(
          value = None,
          arrayValues = None,
          structValues = Some(ListMapLike(List.empty))
        )

      override val `type`: QueryParameterType =
        QueryParameterType(
          `type` = SQLType.Struct,
          arrayType = None,
          structTypes = Some(Nil)
        )
    }

  /** Encode an HList as a struct of parameters, provided each element in the HList can be encoded.
    */
  implicit def hListParameterEncoder[K <: Symbol, H, T <: HList](implicit
      witness: Witness.Aux[K],
      hEncoder: Lazy[ParameterEncoder[H]],
      tEncoder: ParameterEncoder[T]
  ): ParameterEncoder[FieldType[K, H] :: T] =
    new ParameterEncoder[FieldType[K, H] :: T] {

      val name: String = witness.value.name

      override def value(hlist: FieldType[K, H] :: T): QueryParameterValue = {
        val head = hEncoder.value.value(hlist.head)
        val tail = tEncoder.value(hlist.tail)
        val structValues = tail.structValues.map { case ListMapLike(keyValues) =>
          ListMapLike(name -> head :: keyValues)
        }
        tail.copy(structValues = structValues)
      }

      override val `type`: QueryParameterType = {
        val `type`: QueryParameterType = hEncoder.value.`type`
        val tail: QueryParameterType = tEncoder.`type`
        val structTypes = tail.structTypes.map(
          StructType(name = Some(name), `type` = `type`) :: _
        )
        tail.copy(structTypes = structTypes)
      }
    }

  /** Derive a [[ParameterEncoder]] for a generic type.
    *
    * Can derive encoders for some simple types, HLists (so case classes) and array-like types. See methods and values
    * in the companion object of [[ParameterEncoder]] for allowed types.
    */
  implicit def deriveEncoder[A, H](implicit
      generic: LabelledGeneric.Aux[A, H],
      hEncoder: Lazy[ParameterEncoder[H]]
  ): ParameterEncoder[A] =
    new ParameterEncoder[A] {
      override def value(a: A): QueryParameterValue =
        hEncoder.value.value(generic.to(a))

      override val `type`: QueryParameterType = hEncoder.value.`type`
    }
}
