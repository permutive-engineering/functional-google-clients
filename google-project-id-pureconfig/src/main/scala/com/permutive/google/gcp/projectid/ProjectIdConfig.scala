package com.permutive.google.gcp.projectid

import cats.effect.kernel.Concurrent
import cats.syntax.applicative._
import com.permutive.google.gcp.types.ProjectId
import org.http4s.client.Client
import pureconfig.ConfigReader
import pureconfig.generic.auto._
import pureconfig.generic.semiauto._

sealed trait ProjectIdConfig {
  def toProjectId[F[_]: Concurrent](httpClient: Client[F]): F[ProjectId] =
    this match {
      case ProjectIdConfig.Static(value) => value.pure[F]
      case ProjectIdConfig.Gcp =>
        ProjectIdResolver.fromInstanceMetadata(httpClient)
    }
}

object ProjectIdConfig {
  implicit val projectIdReader: ConfigReader[ProjectId] =
    ConfigReader.fromStringOpt(ProjectId.fromString)

  case class Static(value: ProjectId) extends ProjectIdConfig
  case object Gcp extends ProjectIdConfig

  implicit val reader: ConfigReader[ProjectIdConfig] =
    deriveReader[ProjectIdConfig]
}
