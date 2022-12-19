package com.permutive.testutils

import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import munit.FunSuite

trait ResourceSupportMatchers {
  self: FunSuite with ResourceSupport =>

  def checkEncodeDecode[T: Decoder: Encoder](
      fileName: String,
      testObject: T
  ) = {
    checkEncode(fileName, testObject)
    checkDecode(fileName, testObject)
  }

  def checkDecode[T: Decoder](fileName: String, testObject: T) = {
    val testDecode = readJsonResource(fileName).as[T]

    assert(testDecode.isRight)
    val Right(res) = testDecode

    assertEquals(res, testObject)
  }

  def checkEncode[T: Encoder](fileName: String, testObject: T) =
    equalJsonResource(fileName)(testObject.asJson)

}
