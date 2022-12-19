package com.permutive.google.bigquery.datatransfer.models.api

import java.time.Instant

import com.permutive.google.bigquery.datatransfer.models.NewTypes._
import com.permutive.google.bigquery.models.NewTypes._
import com.permutive.google.bigquery.models._
import com.permutive.google.bigquery.models.table.Field
import com.permutive.google.bigquery.models.table.NewTypes.TableId
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}
import io.estatico.newtype.ops._

private[datatransfer] case class ScheduleQueryRequestApi(
  displayName: DisplayName,
  destinationDatasetId: DatasetId,
  params: ScheduleQueryParamsApi,
  schedule: Schedule,
)

// https://cloud.google.com/bigquery/docs/reference/datatransfer/rest/v1/projects.locations.transferConfigs#TransferConfig

sealed private[datatransfer] trait TransferConfigsResponseApi {
  def name: TransferConfigName
  def displayName: DisplayName
  def destinationDatasetId: DatasetId
  def dataSourceId: String
  def updateTime: Instant
  def nextRunTime: Option[Instant]
  def userId: Option[String] // This is really an int though
  def datasetRegion: String
}

final private[datatransfer] case class DfpDtResponseApi(
  name: TransferConfigName,
  displayName: DisplayName,
  destinationDatasetId: DatasetId,
  dataSourceId: String,
  updateTime: Instant,
  nextRunTime: Option[Instant],
  userId: Option[String], // This is really an int though
  datasetRegion: String,
) extends TransferConfigsResponseApi

private[datatransfer] case class ScheduledQueryResponseApi(
  name: TransferConfigName,
  displayName: DisplayName,
  destinationDatasetId: DatasetId,
  params: ScheduleQueryParamsApi,
  schedule: Schedule,
  dataSourceId: String,
  updateTime: Instant,
  nextRunTime: Option[Instant],
  userId: Option[String], // This is really an int though
  datasetRegion: String,
) extends TransferConfigsResponseApi

// These must be optional to support scheduled queries that exist in the console as a whole
// We can not be sure that they will have them
final private[datatransfer] case class ScheduleQueryParamsApi(
  // This nested object has snake case, but the parents do not
  query: Query,
  `destination_table_name_template`: Option[TableId],
  `write_disposition`: Option[WriteDisposition],
  `partitioning_field`: Option[Field.Name],
)

final private[datatransfer] case class ScheduleQueryPatchApi(params: ScheduleQueryParamsApi)

final private[api] case class TransferConfigName(project: BigQueryProjectId, configId: ConfigId)
private[api] object TransferConfigName {

  private[this] val Projects        = "projects"
  private[this] val Locations       = "locations"
  private[this] val TransferConfigs = "transferConfigs"

  // Documentation: https://cloud.google.com/bigquery/docs/reference/datatransfer/rest/v1/projects.locations.transferConfigs#TransferConfig
  def from(s: String): Either[String, TransferConfigName] =
    s.split("/").toList match {
      case Projects :: projectId :: TransferConfigs :: configId :: Nil =>
        Right(TransferConfigName(projectId.coerce[BigQueryProjectId], configId.coerce[ConfigId]))
      case Projects :: projectId :: Locations :: _ :: TransferConfigs :: configId :: Nil =>
        Right(TransferConfigName(projectId.coerce[BigQueryProjectId], configId.coerce[ConfigId]))
      case _ => Left(s"Invalid ScheduledQueryName received in response: `$s`")
    }

  implicit val decoder: Decoder[TransferConfigName] = Decoder[String].emap(from)

}

private[datatransfer] object ScheduleQueryParamsApi {
  implicit val decoder: Decoder[ScheduleQueryParamsApi]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[ScheduleQueryParamsApi] = deriveEncoder
}

private[datatransfer] object ScheduleQueryPatchApi {
  implicit val decoder: Decoder[ScheduleQueryPatchApi]          = deriveDecoder
  implicit val encoder: Encoder.AsObject[ScheduleQueryPatchApi] = deriveEncoder
}

private[datatransfer] object ScheduleQueryRequestApi {
  implicit val encoder: Encoder.AsObject[ScheduleQueryRequestApi] =
    deriveEncoder[ScheduleQueryRequestApi]
      .mapJsonObject(_.add("dataSourceId", Json.fromString("scheduled_query")))
}

private[datatransfer] object TransferConfigsResponseApi {
  implicit val decoder: Decoder[TransferConfigsResponseApi] =
    deriveDecoder[ScheduledQueryResponseApi]
      .or(deriveDecoder[DfpDtResponseApi].map(identity))
}
