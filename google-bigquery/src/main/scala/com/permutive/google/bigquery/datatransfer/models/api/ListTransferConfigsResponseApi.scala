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

package com.permutive.google.bigquery.datatransfer.models.api

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class ListTransferConfigsResponseApi(
    transferConfigs: Option[List[TransferConfigsResponseApi]],
    nextPageToken: Option[
      String
    ] // If this exists it means there are subsequent pages in the response
) extends PaginatedApi {
  val extractScheduledQueries: List[ScheduledQueryResponseApi] =
    transferConfigs.getOrElse(List.empty).collect {
      case sq: ScheduledQueryResponseApi => sq
    }
}

object ListTransferConfigsResponseApi {
  implicit val decoder: Decoder[ListTransferConfigsResponseApi] = deriveDecoder
}
