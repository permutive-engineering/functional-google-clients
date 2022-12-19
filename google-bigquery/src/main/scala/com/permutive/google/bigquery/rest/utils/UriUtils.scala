package com.permutive.google.bigquery.rest.utils

import com.permutive.google.bigquery.rest.models.job.results.NewTypes.PageToken
import org.http4s.Uri

private[rest] object UriUtils {

  def uriWithPageToken(uri: Uri, pageTokenO: Option[PageToken]): Uri =
    fold[PageToken](uri, "pageToken", pageTokenO, _.value)

  def uriWithMaxResults(uri: Uri, maxResultsO: Option[Int]): Uri =
    fold[Int](uri, "maxResults", maxResultsO, _.toString)

  private def fold[A](uri: Uri, paramName: String, oa: Option[A], asString: A => String): Uri =
    oa.fold(uri)(a => uri.withQueryParam(paramName, asString(a)))

}
