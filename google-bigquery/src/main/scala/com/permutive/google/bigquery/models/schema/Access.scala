package com.permutive.google.bigquery.models.schema

import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}

sealed trait Access {
  def role: String
}

object Access {
  final case class UserByEmail(role: String, userByEmail: String)   extends Access
  final case class GroupByEmail(role: String, groupByEmail: String) extends Access
  final case class Domain(role: String, domain: String)             extends Access
  final case class SpecialGroup(role: String, specialGroup: String) extends Access
  final case class IamMember(role: String, iamMember: String)       extends Access

  def userByEmail(role: String, userByEmail: String): Access   = UserByEmail(role, userByEmail)
  def groupByEmail(role: String, groupByEmail: String): Access = GroupByEmail(role, groupByEmail)
  def domain(role: String, domain: String): Access             = Domain(role, domain)
  def specialGroup(role: String, specialGroup: String): Access = SpecialGroup(role, specialGroup)
  def iamMember(role: String, iamMember: String): Access       = IamMember(role, iamMember)

  implicit val encoder: Encoder.AsObject[Access] = Encoder.encodeJsonObject.contramapObject {
    case a @ UserByEmail(_, _)  => deriveEncoder[UserByEmail].encodeObject(a)
    case a @ GroupByEmail(_, _) => deriveEncoder[GroupByEmail].encodeObject(a)
    case a @ Domain(_, _)       => deriveEncoder[Domain].encodeObject(a)
    case a @ SpecialGroup(_, _) => deriveEncoder[SpecialGroup].encodeObject(a)
    case a @ IamMember(_, _)    => deriveEncoder[IamMember].encodeObject(a)
  }

  private def widen[A <: Access](decoder: Decoder[A]): Decoder[Access] = decoder.asInstanceOf[Decoder[Access]]

  implicit val decoder: Decoder[Access] =
    widen(deriveDecoder[UserByEmail])
      .or(widen(deriveDecoder[GroupByEmail]))
      .or(widen(deriveDecoder[Domain]))
      .or(widen(deriveDecoder[SpecialGroup]))
      .or(widen(deriveDecoder[IamMember]))
}
