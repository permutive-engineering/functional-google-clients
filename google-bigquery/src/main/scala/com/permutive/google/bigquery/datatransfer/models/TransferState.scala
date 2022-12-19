package com.permutive.google.bigquery.datatransfer.models

import enumeratum.EnumEntry.UpperSnakecase
import enumeratum.{CirceEnum, Enum, EnumEntry}

import scala.collection.immutable

// Here as it may be exposed "publicly" at some point
// Documentation: https://cloud.google.com/bigquery/docs/reference/datatransfer/rest/v1/TransferState
sealed trait TransferState extends EnumEntry with UpperSnakecase
object TransferState extends Enum[TransferState] with CirceEnum[TransferState] {
  override val values: immutable.IndexedSeq[TransferState] = findValues

  case object TransferStateUnspecified extends TransferState
  case object Pending                  extends TransferState
  case object Running                  extends TransferState
  case object Succeeded                extends TransferState
  case object Failed                   extends TransferState
  case object Cancelled                extends TransferState
}
