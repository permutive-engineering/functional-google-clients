package com.permutive.google.bigquery.http

import cats.Applicative
import cats.effect.kernel.Temporal
import cats.syntax.all._
import com.permutive.google.auth.oauth.models.AccessToken
import com.permutive.google.bigquery.configuration.RetryConfiguration
import com.permutive.google.bigquery.models.Exceptions.{FailedRequest, RequestEntityNotFound}
import fs2.Stream
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Authorization
import org.http4s._
import org.typelevel.ci.CIString

trait HttpMethods[F[_]] {

  def sendAuthorizedRequest[T](request: Request[F], description: => String)(implicit ed: EntityDecoder[F, T]): F[T]

  def sendRequest[T](request: Request[F], description: => String)(implicit ed: EntityDecoder[F, T]): F[T]

  def sendAuthorizedGet[T](uri: Uri, description: => String)(implicit ed: EntityDecoder[F, T]): F[T]

  def sendGet[T](uri: Uri, description: => String)(implicit ed: EntityDecoder[F, T]): F[T]

}

object HttpMethods {

  def apply[F[_]: HttpMethods]: HttpMethods[F] = implicitly

  def impl[F[_]: Temporal](
    client: Client[F],
    tokenF: F[AccessToken],
    retryConfiguration: Option[RetryConfiguration] = None,
  ): HttpMethods[F] =
    new HttpMethods[F] {
      object Dsl extends Http4sDsl[F] with Http4sClientDsl[F]
      import Dsl._

      private def retry[A](fa: F[A]): F[A] =
        retryConfiguration match {
          case Some(conf) if conf.maxAttempts > 0 =>
            Stream.retry(fa, conf.delay, conf.nextDelay, conf.maxAttempts, conf.retriable).compile.lastOrError
          case _ => fa
        }

      override def sendAuthorizedRequest[T](request: Request[F], description: => String)(implicit
        ed: EntityDecoder[F, T],
      ): F[T] =
        for {
          req <- authorize(request)
          res <- sendRequest[T](req, description)
        } yield res

      override def sendRequest[T](request: Request[F], description: => String)(implicit ed: EntityDecoder[F, T]): F[T] =
        retry(client.expectOr[T](request)(failedRequest(description)))

      override def sendAuthorizedGet[T](uri: Uri, description: => String)(implicit ed: EntityDecoder[F, T]): F[T] =
        for {
          req <- authorize(GET(uri))
          res <- getRequest[T](req, description)
        } yield res

      override def sendGet[T](uri: Uri, description: => String)(implicit ed: EntityDecoder[F, T]): F[T] =
        getRequest(GET(uri), description)

      private def getRequest[T](request: Request[F], description: => String)(implicit ed: EntityDecoder[F, T]): F[T] =
        retry(
          client.expectOr[T](request) { resp =>
            resp.status match {
              case NotFound => RequestEntityNotFound(description).pure.widen
              case _        => failedRequest(description)(resp)
            }
          },
        )

      private def failedRequest(description: => String)(resp: Response[F]): F[Throwable] =
        EntityDecoder.decodeText(resp).map(FailedRequest(resp.status.code, description, _))

      private def authorize(req: Request[F]): F[Request[F]] =
        tokenF.map(authorizeRequest(req, _))

      private def authorizeRequest(req: Request[F], token: AccessToken): Request[F] =
        req.putHeaders(
          Authorization(Credentials.Token(CIString(token.tokenType.value), token.accessToken.value)),
        )
    }

  def create[F[_]: Temporal](
    client: Client[F],
    tokenF: F[AccessToken],
    retryConfiguration: Option[RetryConfiguration] = None,
  ): F[HttpMethods[F]] =
    Applicative[F].pure(impl(client, tokenF, retryConfiguration))

}
