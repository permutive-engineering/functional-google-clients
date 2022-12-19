package com.permutive.google.bigquery.models.table

import enumeratum.EnumEntry.Uppercase
import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed trait TableType extends EnumEntry with Uppercase
object TableType extends Enum[TableType] with CirceEnum[TableType] {
  override val values = findValues

  case object Table    extends TableType
  case object View     extends TableType
  case object External extends TableType
}
