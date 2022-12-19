package com.permutive.google.bigquery.rest.models.job.queryparameters

case class SimpleCaseClass(foo: String, bar: String, baz: Int, qux: Double, blib: Boolean)

case class ArrayCaseClass(foo: String, bar: List[String])

case class ArrayArrayCaseClass(foo: List[List[Int]])

case class NestedCaseClass(foo: Int, bar: SimpleCaseClass, baz: ArrayCaseClass)
