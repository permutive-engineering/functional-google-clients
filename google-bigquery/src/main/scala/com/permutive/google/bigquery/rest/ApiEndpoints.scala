package com.permutive.google.bigquery.rest

import org.http4s.Uri

private[rest] object ApiEndpoints {

  // API Documentation: https://cloud.google.com/bigquery/docs/reference/rest/v2/

  val baseRestUri = Uri.unsafeFromString("https://www.googleapis.com/bigquery/v2")

}
