/*
 * Copyright 2022 Permutive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.permutive.google.bigquery.models

object Exceptions {

  trait BigQueryException extends Throwable

  sealed abstract class FailedRequest private (val code: Int, val description: String, val body: String)
      extends RuntimeException(
        s"Failed request to $description, got response code: $code; body: $body"
      )
      with BigQueryException

  object FailedRequest {
    def apply(code: Int, description: String, body: String): FailedRequest =
      new FailedRequest(code, description, body) {}
  }

  sealed abstract class RequestEntityNotFound private (val description: String)
      extends RuntimeException(
        s"Failed request to $description because the entity was not found"
      )
      with BigQueryException

  object RequestEntityNotFound {
    def apply(description: String): RequestEntityNotFound = new RequestEntityNotFound(description) {}

  }

}
