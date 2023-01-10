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

package com.permutive.google.auth.oauth.user.crypto

import java.nio.file.{Files, Path}

import cats.Eq
import cats.effect.Sync
import cats.syntax.all._
import com.permutive.google.auth.oauth.user.models.NewTypes._
import io.circe
import io.circe.parser

object GoogleClientSecretsParser {
  sealed abstract class GoogleUserAccount(val clientId: ClientId, val clientSecret: ClientSecret) {
    override def equals(obj: Any): Boolean = obj match {
      case other: GoogleUserAccount => Eq[GoogleUserAccount].eqv(this, other)
      case _ => false
    }
  }

  object GoogleUserAccount {
    private[crypto] def apply(clientId: ClientId, clientSecret: ClientSecret): GoogleUserAccount =
      new GoogleUserAccount(clientId, clientSecret) {}

    implicit val eq: Eq[GoogleUserAccount] = Eq.instance { (x, y) =>
      x.clientId == y.clientId && x.clientSecret == y.clientSecret
    }
  }

  final def parse[F[_]](
      path: Path
  )(implicit F: Sync[F]): F[GoogleUserAccount] = {
    def parseAccount(string: String): Either[circe.Error, GoogleUserAccount] = for {
      json <- parser.parse(string)
      cursor = json.hcursor.downField("installed")
      clientId <- cursor.get[ClientId]("client_id")
      clientSecret <- cursor.get[ClientSecret]("client_secret")
    } yield GoogleUserAccount(
      clientId,
      clientSecret
    )

    for {
      bytes <- F.blocking(Files.readAllBytes(path))
      string <- F.delay(new String(bytes))
      account <- parseAccount(string).liftTo[F]
    } yield account
  }

}
