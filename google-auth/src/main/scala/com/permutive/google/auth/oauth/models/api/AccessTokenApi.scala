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

package com.permutive.google.auth.oauth.models.api

import com.github.plokhotnyuk.jsoniter_scala.core.JsonValueCodec
import com.github.plokhotnyuk.jsoniter_scala.macros.{
  CodecMakerConfig,
  JsonCodecMaker
}
import com.permutive.google.auth.oauth.models.AccessToken._

private[oauth] case class AccessTokenApi(
    accessToken: Token,
    tokenType: TokenType,
    expiresIn: ExpiresIn
)

private[oauth] object AccessTokenApi {

  implicit final val codec: JsonValueCodec[AccessTokenApi] =
    JsonCodecMaker.make[AccessTokenApi](
      CodecMakerConfig.withFieldNameMapper(fieldNameMapper =
        JsonCodecMaker.enforce_snake_case
      )
    )

}
