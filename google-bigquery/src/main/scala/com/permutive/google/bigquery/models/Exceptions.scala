package com.permutive.google.bigquery.models

object Exceptions {

  trait BigQueryException extends Throwable

  case class FailedRequest(code: Int, description: String, body: String)
      extends RuntimeException(s"Failed request to $description, got response code: $code; body: $body")
      with BigQueryException

  case class RequestEntityNotFound(description: String)
      extends RuntimeException(s"Failed request to $description because the entity was not found")
      with BigQueryException

}
