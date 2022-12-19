package com.permutive.google.bigquery.rest.models.job

/**
  * Settings to control pagination of results from BigQuery.
  *
  * @param prefetchPages How many pages of results to prefetch (to prevent fetching blocking processing)
  * @param maxResultsPerPage The maximum number of rows to return in each page of results (response from BigQuery)
  *
  * Notes:
  *
  * Assuming constant fetch page fetch and page processing time then `1` is the optimum value for [[prefetchPages]].
  * This is because pages cannot be fetched in parallel.
  *
  * Even if no [[maxResultsPerPage]] is specified BigQuery imposes a 10 MB limit on response sizes automatically:
  * https://cloud.google.com/bigquery/docs/reference/rest/v2/jobs/query#QueryRequest
  */
case class PaginationSettings(
  prefetchPages: Int,
  maxResultsPerPage: Option[Int],
) {

  def withPrefetchPages(prefetchPages: Int): PaginationSettings =
    copy(prefetchPages = prefetchPages)

  def withMaxResultsPerPage(maxResultsPerPage: Int): PaginationSettings =
    copy(maxResultsPerPage = Some(maxResultsPerPage))

}

object PaginationSettings {

  /**
    * Default value for [[PaginationSettings]]. 1 page prefetched and no maximum number of results.
    *
    * A single prefetched page is the theoretical optimum assuming constant page fetch and page processing time.
    *
    * No max results per page will still impose the BigQuery limit of 10 MB per page:
    * https://cloud.google.com/bigquery/docs/reference/rest/v2/jobs/query#QueryRequest
    */
  val default: PaginationSettings =
    PaginationSettings(
      prefetchPages = 1,
      maxResultsPerPage = None,
    )

}
