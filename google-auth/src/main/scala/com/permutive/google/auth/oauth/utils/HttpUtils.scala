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

package com.permutive.google.auth.oauth.utils

import cats.effect.kernel.Async
import cats.syntax.all._
import com.github.plokhotnyuk.jsoniter_scala.core.readFromArray
import com.permutive.google.auth.oauth.models.FailedRequest
import com.permutive.google.auth.oauth.models.api.AccessTokenApi
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Request}
import org.typelevel.log4cats.Logger

private[oauth] object HttpUtils {

  def fetchAccessTokenApi[F[_]: Logger](
      client: Client[F],
      request: Request[F],
      description: => String
  )(implicit
      F: Async[F]
  ): F[Option[AccessTokenApi]] =
    client
      .expectOr[Array[Byte]](request) { resp =>
        EntityDecoder
          .decodeText(resp)
          .map(FailedRequest(s"retrieve $description", _))
      }
      .flatMap(bytes =>
        F.delay(readFromArray[AccessTokenApi](bytes)).map(_.some)
      )
      .handleErrorWith { e =>
        Logger[F].warn(e)(
          s"Failed to retrieve $description Access Token from Google"
        ) >> F.pure(None)
      }

}
