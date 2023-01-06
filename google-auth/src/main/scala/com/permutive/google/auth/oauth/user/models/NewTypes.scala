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

package com.permutive.google.auth.oauth.user.models

import io.circe.Decoder

object NewTypes {

  case class ClientId(value: String) extends AnyVal
  object ClientId {
    implicit val decoder: Decoder[ClientId] =
      Decoder.decodeString.map(ClientId(_))
  }

  case class ClientSecret(value: String) extends AnyVal
  object ClientSecret {
    implicit val decoder: Decoder[ClientSecret] =
      Decoder.decodeString.map(ClientSecret(_))
  }

  case class RefreshToken(value: String) extends AnyVal
  object RefreshToken {
    implicit val decoder: Decoder[RefreshToken] =
      Decoder.decodeString.map(RefreshToken(_))
  }
}
