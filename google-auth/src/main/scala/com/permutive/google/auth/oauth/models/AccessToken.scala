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

package com.permutive.google.auth.oauth.models

import com.permutive.google.auth.oauth.models.AccessToken._
import io.circe.Decoder

sealed trait AccessToken {
  def accessToken: Token
  def tokenType: TokenType
  def expiresIn: ExpiresIn
}

object AccessToken {
  case class Token(value: String) extends AnyVal
  object Token {
    implicit val decoder: Decoder[Token] = Decoder.decodeString.map(Token(_))
  }

  case class TokenType(value: String) extends AnyVal
  object TokenType {
    implicit val decoder: Decoder[TokenType] =
      Decoder.decodeString.map(TokenType(_))
  }

  case class ExpiresIn(value: Int) extends AnyVal
  object ExpiresIn {
    implicit val decoder: Decoder[ExpiresIn] =
      Decoder.decodeInt.map(ExpiresIn(_))
  }
}

final case class ServiceAccountAccessToken(
    accessToken: Token,
    tokenType: TokenType,
    expiresIn: ExpiresIn
) extends AccessToken

final case class UserAccountAccessToken(
    accessToken: Token,
    tokenType: TokenType,
    expiresIn: ExpiresIn
) extends AccessToken
