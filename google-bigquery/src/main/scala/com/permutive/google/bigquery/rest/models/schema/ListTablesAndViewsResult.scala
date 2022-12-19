package com.permutive.google.bigquery.rest.models.schema

import com.permutive.google.bigquery.rest.models.job.results.NewTypes.PageToken

case class ListTablesAndViewsResult(
  objects: List[DatasetObject],
  totalObjects: Int,
  nextPageToken: Option[PageToken],
)
