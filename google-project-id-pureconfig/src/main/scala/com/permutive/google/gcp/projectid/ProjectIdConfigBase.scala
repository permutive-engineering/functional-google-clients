package com.permutive.google.gcp.projectid

import cats.effect.kernel.Concurrent
import cats.syntax.applicative._
import com.permutive.google.gcp.types.ProjectId
import org.http4s.client.Client

private[projectid] trait ProjectIdConfigBase {
  def toProjectId[F[_]: Concurrent](httpClient: Client[F]): F[ProjectId] =
    this match {
      case ProjectIdConfig.Static(value) => value.pure[F]
      case ProjectIdConfig.Gcp =>
        ProjectIdResolver.fromInstanceMetadata(httpClient)
    }
}
