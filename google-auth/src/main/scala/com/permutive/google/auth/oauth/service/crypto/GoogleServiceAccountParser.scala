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
import com.permutive.google.auth.oauth.service.models.GoogleServiceAccount
import io.circe.Decoder
import io.circe.parser._

object GoogleServiceAccountParser {

  case class JsonGoogleServiceAccount(
      `type`: String,
      projectId: String,
      privateKeyId: String,
      privateKey: String,
      clientEmail: String,
      authUri: String
  )

  object JsonGoogleServiceAccount {
    implicit final val decoder: Decoder[JsonGoogleServiceAccount] =
      Decoder.forProduct6("type", "project_id", "private_key_id", "private_key", "client_email", "auth_uri")(
        JsonGoogleServiceAccount.apply
      )
  }

  final def parse[F[_]](
      path: Path
  )(implicit F: Sync[F]): F[GoogleServiceAccount] =
    for {
      bytes <- F.blocking(Files.readAllBytes(path))
      string <- F.delay(new String(bytes))
      sa <- decode[JsonGoogleServiceAccount](string).liftTo[F]
      pem <- loadPem(sa.privateKey)
      spec <- F.delay(new PKCS8EncodedKeySpec(pem))
      kf <- F.delay(KeyFactory.getInstance("RSA"))
      key <- F.delay(kf.generatePrivate(spec).asInstanceOf[RSAPrivateKey])
    } yield GoogleServiceAccount(
      clientEmail = sa.clientEmail,
      privateKey = key
    )

  final private[this] val privateKeyPattern =
    Pattern.compile("(?m)(?s)^---*BEGIN.*---*$(.*)^---*END.*---*$.*")

  private def loadPem[F[_]](pem: String)(implicit F: Sync[F]): F[Array[Byte]] =
    for {
      encoded <- F.delay(privateKeyPattern.matcher(pem).replaceFirst("$1"))
      bytes <- F.delay(Base64.getMimeDecoder.decode(encoded))
    } yield bytes

}
