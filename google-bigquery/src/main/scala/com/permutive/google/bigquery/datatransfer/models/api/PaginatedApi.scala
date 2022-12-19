package com.permutive.google.bigquery.datatransfer.models.api

trait PaginatedApi {
  def nextPageToken: Option[String]
}
