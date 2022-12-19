package com.permutive.google.bigquery.models

import enumeratum.{CirceEnum, Enum, EnumEntry}
import enumeratum.EnumEntry.UpperSnakecase

// Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/jobs

sealed trait WriteDisposition extends EnumEntry with UpperSnakecase
object WriteDisposition extends Enum[WriteDisposition] with CirceEnum[WriteDisposition] {
  override val values = findValues

  case object WriteTruncate extends WriteDisposition
  case object WriteAppend   extends WriteDisposition

  //    // Commented out as unsupported here (error handling needed) but is supported by Google
  //    case object WriteEmpty    extends WriteDisposition
}
