package com.permutive.google.bigquery.rest.models

import com.permutive.google.bigquery.models.NewTypes.{BigQueryProjectName, DatasetId}
import com.permutive.google.bigquery.models.table.NewTypes.TableId
import com.permutive.google.bigquery.rest.models.api.TableReferenceApi
import io.scalaland.chimney.dsl._

case class TableReference(
  projectId: BigQueryProjectName,
  dataset: DatasetId,
  name: TableId,
)

object TableReference {

  private[rest] def fromApi(api: TableReferenceApi): TableReference =
    api.into[TableReference].withFieldRenamed(_.datasetId, _.dataset).withFieldRenamed(_.tableId, _.name).transform

}
