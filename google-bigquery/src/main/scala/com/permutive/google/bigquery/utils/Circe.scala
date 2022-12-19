package com.permutive.google.bigquery.utils

import io.circe.{Encoder, Printer}
import org.http4s.EntityEncoder
import org.http4s.circe.CirceInstances

private[bigquery] object Circe {

  // Have to manually specify this to ensure that nulls are removed
  val printerDropNullValues: Printer =
    Printer.noSpaces.copy(dropNullValues = true)

  private[this] val circeInstance: CirceInstances =
    CirceInstances.withPrinter(printerDropNullValues).build

  def circeEntityEncoderDropNullValues[F[_], T: Encoder]: EntityEncoder[F, T] =
    circeInstance.jsonEncoderOf[F, T]

}
