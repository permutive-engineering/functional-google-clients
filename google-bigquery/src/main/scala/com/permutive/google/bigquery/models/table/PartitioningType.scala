package com.permutive.google.bigquery.models.table

import enumeratum.EnumEntry.Uppercase
import enumeratum.{CirceEnum, Enum, EnumEntry}

sealed trait PartitioningType extends EnumEntry with Uppercase
object PartitioningType extends Enum[PartitioningType] with CirceEnum[PartitioningType] {
  override val values = findValues

  case object Day   extends PartitioningType
  case object Hour  extends PartitioningType
  case object Month extends PartitioningType
  case object Year  extends PartitioningType
}
