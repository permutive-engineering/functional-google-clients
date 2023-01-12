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

package com.permutive.google.auth.oauth.service.crypto

import java.nio.file.{Files, Path}
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import java.util.regex.Pattern

import cats.effect.Sync
import cats.syntax.all._
import io.circe
import io.circe.parser

object GoogleServiceAccountParser {
  sealed abstract class GoogleServiceAccount private (val clientEmail: String, val privateKey: RSAPrivateKey)

  object GoogleServiceAccount {
    private[crypto] def apply(clientEmail: String, privateKey: RSAPrivateKey): GoogleServiceAccount =
      new GoogleServiceAccount(clientEmail, privateKey) {}
  }

  final def parse[F[_]](
      path: Path
  )(implicit F: Sync[F]): F[GoogleServiceAccount] = {
    def parseJson(string: String): Either[circe.Error, (String, String)] = for {
      json <- parser.parse(string)
      privateKey <- json.hcursor.get[String]("private_key")
      clientEmail <- json.hcursor.get[String]("client_email")
    } yield (privateKey, clientEmail)

    for {
      bytes <- F.blocking(Files.readAllBytes(path))
      string <- F.delay(new String(bytes))
      keyEmail <- parseJson(string).liftTo[F]
      pem <- loadPem(keyEmail._1)
      spec <- F.delay(new PKCS8EncodedKeySpec(pem))
      kf <- F.delay(KeyFactory.getInstance("RSA"))
      key <- F.delay(kf.generatePrivate(spec).asInstanceOf[RSAPrivateKey])
    } yield GoogleServiceAccount(
      clientEmail = keyEmail._2,
      privateKey = key
    )
  }

  final private[this] val privateKeyPattern =
    Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*")

  private def loadPem[F[_]](pem: String)(implicit F: Sync[F]): F[Array[Byte]] =
    for {
      encoded <- F.delay(privateKeyPattern.matcher(pem).replaceFirst("$1"))
      bytes <- F.delay(Base64.getMimeDecoder.decode(encoded))
    } yield bytes

}
