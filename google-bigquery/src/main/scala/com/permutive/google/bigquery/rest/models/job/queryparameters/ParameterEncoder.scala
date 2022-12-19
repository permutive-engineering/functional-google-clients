package com.permutive.google.bigquery.rest.models.job.queryparameters

import cats.syntax.foldable._
import cats.syntax.functor._
import cats.{Contravariant, Foldable, Functor}
import com.permutive.google.bigquery.models.SQLType
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

/** Allows encoding to a named BigQuery query parameter. */
trait QueryParameterEncoder[A] {

  /**
    * @param name The name of the query parameter (as defined in the query)
    * @param value The data to supply
    * @return The encoded query parameter
    */
  def encode(name: String, value: A): QueryParameter
}

object QueryParameterEncoder {

  def apply[A](implicit qpe: QueryParameterEncoder[A]): QueryParameterEncoder[A] = qpe

  implicit val contravariantForQueryParameterEncoder: Contravariant[QueryParameterEncoder] =
    new Contravariant[QueryParameterEncoder] {
      override def contramap[A, B](fa: QueryParameterEncoder[A])(f: B => A): QueryParameterEncoder[B] = {
        case (name, value) => fa.encode(name, f(value))
      }
    }

  /**
    * Derive a [[QueryParameterEncoder]] for a generic type.
    *
    * This method is implicit so does not need to be used directly ([[apply]] can be used to summon an instance).
    * You may wish to use this method directly to use the same [[QueryParameterEncoder]] in multiple locations without
    * deriving a new instance in each location.
    *
    * Can derive encoders for some simple types, HLists (so case classes) and array-like types.
    * See methods and values in the companion object of [[ParameterEncoder]] for allowed types.
    */
  implicit def deriveEncoder[A](implicit
    hEnc: Lazy[ParameterEncoder[A]],
  ): QueryParameterEncoder[A] =
    (name, a) =>
      QueryParameter(name = Some(name), parameterType = hEnc.value.`type`, parameterValue = hEnc.value.value(a))

}

/**
  *  Allows encoding to BigQuery query parameter type and value.
  *
  * Mostly used internally for generic derivation. See [[QueryParameterEncoder]] for public use.
  */
trait ParameterEncoder[A] {
  def value(a: A): QueryParameterValue
  val `type`: QueryParameterType
}

object ParameterEncoder {

  def apply[A](implicit p: ParameterEncoder[A]): ParameterEncoder[A] = p

  implicit val intParameterEncoder: ParameterEncoder[Int] = new ParameterEncoder[Int] {
    override def value(i: Int): QueryParameterValue = QueryParameterValue.singular(i.toString)
    override val `type`: QueryParameterType         = QueryParameterType.singular(SQLType.Int64)
  }

  implicit val stringParameterEncoder: ParameterEncoder[String] = new ParameterEncoder[String] {
    override def value(s: String): QueryParameterValue = QueryParameterValue.singular(s)
    override val `type`: QueryParameterType            = QueryParameterType.singular(SQLType.String)
  }

  implicit val doubleParameterEncoder: ParameterEncoder[Double] = new ParameterEncoder[Double] {
    override def value(s: Double): QueryParameterValue = QueryParameterValue.singular(s.toString)
    override val `type`: QueryParameterType            = QueryParameterType.singular(SQLType.Float64)
  }

  implicit val booleanParameterEncoder: ParameterEncoder[Boolean] = new ParameterEncoder[Boolean] {
    override def value(b: Boolean): QueryParameterValue = QueryParameterValue.singular(b.toString)
    override val `type`: QueryParameterType             = QueryParameterType.singular(SQLType.Boolean)
  }

  /**
    * Encode a `List[A]` as an array of parameters, provided `A` can be encoded.
    *
    * Helper method around [[foldableParameterEncoder]] to avoid importing list instances.
    */
  implicit def listParameterEncoder[A: ParameterEncoder]: ParameterEncoder[List[A]] =
    foldableParameterEncoder

  /** Encode an `F[A]` as an array of parameters, provided constraints on `F[_]` are met and `A` can be encoded. */
  implicit def foldableParameterEncoder[A: ParameterEncoder, F[_]: Foldable: Functor]: ParameterEncoder[F[A]] =
    new ParameterEncoder[F[A]] {
      override def value(values: F[A]): QueryParameterValue =
        QueryParameterValue(
          value = None,
          arrayValues = Some(values.map(ParameterEncoder[A].value).toList),
          structValues = None,
        )
      override val `type`: QueryParameterType =
        QueryParameterType(`type` = SQLType.Array, arrayType = Some(ParameterEncoder[A].`type`), structTypes = None)
    }

  /**
    * Encode an empty HList as an empty struct parameter
    *
    * Used in derivation as the seed for encoding structs.
    */
  implicit val hNilParameterEncoder: ParameterEncoder[HNil] = new ParameterEncoder[HNil] {
    override def value(hnil: HNil): QueryParameterValue =
      QueryParameterValue(value = None, arrayValues = None, structValues = Some(ListMapLike(List.empty)))
    override val `type`: QueryParameterType =
      QueryParameterType(`type` = SQLType.Struct, arrayType = None, structTypes = Some(Nil))
  }

  /** Encode an HList as a struct of parameters, provided each element in the HList can be encoded. */
  implicit def hListParameterEncoder[K <: Symbol, H, T <: HList](implicit
    witness: Witness.Aux[K],
    hEncoder: Lazy[ParameterEncoder[H]],
    tEncoder: ParameterEncoder[T],
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
        val tail: QueryParameterType   = tEncoder.`type`
        val structTypes                = tail.structTypes.map(StructType(name = Some(name), `type` = `type`) :: _)
        tail.copy(structTypes = structTypes)
      }
    }

  /**
    * Derive a [[ParameterEncoder]] for a generic type.
    *
    * Can derive encoders for some simple types, HLists (so case classes) and array-like types.
    * See methods and values in the companion object of [[ParameterEncoder]] for allowed types.
    */
  implicit def deriveEncoder[A, H](implicit
    generic: LabelledGeneric.Aux[A, H],
    hEncoder: Lazy[ParameterEncoder[H]],
  ): ParameterEncoder[A] =
    new ParameterEncoder[A] {
      override def value(a: A): QueryParameterValue = hEncoder.value.value(generic.to(a))
      override val `type`: QueryParameterType       = hEncoder.value.`type`
    }

  implicit val contravariantForParameterEncoder: Contravariant[ParameterEncoder] = new Contravariant[ParameterEncoder] {
    override def contramap[A, B](fa: ParameterEncoder[A])(f: B => A): ParameterEncoder[B] =
      new ParameterEncoder[B] {
        override def value(b: B): QueryParameterValue = fa.value(f(b))
        override val `type`: QueryParameterType       = fa.`type`
      }
  }

}
