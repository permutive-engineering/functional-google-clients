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

package com.permutive.google.bigquery.rest.models.job

/** Settings to control pagination of results from BigQuery.
  *
  * @param prefetchPages
  *   How many pages of results to prefetch (to prevent fetching blocking processing)
  * @param maxResultsPerPage
  *   The maximum number of rows to return in each page of results (response from BigQuery)
  *
  * Notes:
  *
  * Assuming constant fetch page fetch and page processing time then `1` is the optimum value for [[prefetchPages]].
  * This is because pages cannot be fetched in parallel.
  *
  * Even if no [[maxResultsPerPage]] is specified BigQuery imposes a 10 MB limit on response sizes automatically:
  * https://cloud.google.com/bigquery/docs/reference/rest/v2/jobs/query#QueryRequest
  */
sealed abstract class PaginationSettings private (
    val prefetchPages: Int,
    val maxResultsPerPage: Option[Int]
) {

  private def copy(prefetchPages: Int = prefetchPages, maxResultsPerPage: Option[Int] = maxResultsPerPage) =
    new PaginationSettings(prefetchPages, maxResultsPerPage) {}

  def withPrefetchPages(prefetchPages: Int): PaginationSettings =
    copy(prefetchPages = prefetchPages)

  def withMaxResultsPerPage(maxResultsPerPage: Int): PaginationSettings =
    copy(maxResultsPerPage = Some(maxResultsPerPage))

}

object PaginationSettings {

  /** Default value for [[PaginationSettings]]. 1 page prefetched and no maximum number of results.
    *
    * A single prefetched page is the theoretical optimum assuming constant page fetch and page processing time.
    *
    * No max results per page will still impose the BigQuery limit of 10 MB per page:
    * https://cloud.google.com/bigquery/docs/reference/rest/v2/jobs/query#QueryRequest
    */
  val default: PaginationSettings =
    new PaginationSettings(
      prefetchPages = 1,
      maxResultsPerPage = None
    ) {}

}
