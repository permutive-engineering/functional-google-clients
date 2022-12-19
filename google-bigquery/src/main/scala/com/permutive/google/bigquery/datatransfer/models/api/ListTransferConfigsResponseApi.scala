package com.permutive.google.bigquery.datatransfer.models.api

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class ListTransferConfigsResponseApi(
  transferConfigs: Option[List[TransferConfigsResponseApi]],
  nextPageToken: Option[String], // If this exists it means there are subsequent pages in the response
) extends PaginatedApi {
  val extractScheduledQueries: List[ScheduledQueryResponseApi] = transferConfigs.getOrElse(List.empty).collect {
    case sq: ScheduledQueryResponseApi => sq
  }
}

object ListTransferConfigsResponseApi {
  implicit val decoder: Decoder[ListTransferConfigsResponseApi] = deriveDecoder
}
