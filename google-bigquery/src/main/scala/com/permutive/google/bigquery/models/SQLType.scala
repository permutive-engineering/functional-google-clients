package com.permutive.google.bigquery.models

import enumeratum.{CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.Uppercase

sealed trait SQLType extends EnumEntry with Uppercase
object SQLType extends Enum[SQLType] with CirceEnum[SQLType] {
  override val values = findValues

  case object Bytes     extends SQLType
  case object Boolean   extends SQLType
  case object Date      extends SQLType
  case object Time      extends SQLType
  case object Datetime  extends SQLType
  case object Timestamp extends SQLType
  case object Float     extends SQLType
  case object Integer   extends SQLType
  case object String    extends SQLType
  case object Record    extends SQLType
  case object Geography extends SQLType
  case object Numeric   extends SQLType
  case object Array     extends SQLType
  case object Struct    extends SQLType
  case object Int64     extends SQLType
  case object Float64   extends SQLType

  // These may need all of the types from these locations ultimately:
  // https://github.com/googleapis/google-cloud-java/blob/master/google-cloud-clients/google-cloud-bigquery/src/main/java/com/google/cloud/bigquery/StandardSQLTypeName.java
  // https://github.com/googleapis/google-cloud-java/blob/master/google-cloud-clients/google-cloud-bigquery/src/main/java/com/google/cloud/bigquery/LegacySQLTypeName.java
}
