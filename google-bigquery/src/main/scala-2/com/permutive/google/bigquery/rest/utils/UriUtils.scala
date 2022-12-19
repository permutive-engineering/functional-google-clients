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

package com.permutive.google.bigquery.rest.utils

import com.permutive.google.bigquery.rest.models.job.results.NewTypes.PageToken
import org.http4s.Uri

private[rest] object UriUtils {

  def uriWithPageToken(uri: Uri, pageTokenO: Option[PageToken]): Uri =
    fold[PageToken](uri, "pageToken", pageTokenO, _.value)

  def uriWithMaxResults(uri: Uri, maxResultsO: Option[Int]): Uri =
    fold[Int](uri, "maxResults", maxResultsO, _.toString)

  private def fold[A](
      uri: Uri,
      paramName: String,
      oa: Option[A],
      asString: A => String
  ): Uri =
    oa.fold(uri)(a => uri.withQueryParam(paramName, asString(a)))

}
