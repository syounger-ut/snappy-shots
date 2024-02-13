package com.authService.models

import com.authService.utils.Profile
import play.api.libs.json._

import java.time.Instant

case class Photo(
  id: Long,
  title: String,
  description: Option[String] = None,
  source: Option[String] = None,
  fileName: Option[String] = None,
  creator_id: Long,
  created_at: Option[Instant] = None,
  updated_at: Option[Instant] = None
)

object Photo {
  implicit val format: Format[Photo] = Json.format[Photo]
}

trait PhotosTable { this: Profile =>
  import profile.api._

  class PhotosTableDef(tag: Tag) extends Table[Photo](tag, "photos") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def description = column[Option[String]]("description")
    def source = column[Option[String]]("source")
    def fileName: Rep[Option[String]] = column[Option[String]]("file_name")
    def creator_id = column[Long]("creator_id")
    def created_at = column[Option[Instant]]("created_at")
    def updated_at = column[Option[Instant]]("updated_at")

    def * = (
      id,
      title,
      description,
      source,
      fileName,
      creator_id,
      created_at,
      updated_at
    ) <> ((Photo.apply _).tupled, Photo.unapply)
  }

  val photos = TableQuery[PhotosTableDef]
}
