package com.authService.models

import play.api.libs.json._
import slick.jdbc.JdbcProfile

import java.time.Instant

trait Profile {
  val profile: JdbcProfile
}

case class Photo(
  id: Long,
  title: String,
  description: String,
  source: String,
  creator_id: Long,
  created_at: Instant,
  updated_at: Instant
)

object Photo {
  implicit val format: Format[Photo] = Json.format[Photo]
}

trait PhotosTable { this: Profile =>
  import profile.api._

  class PhotosTableDef(tag: Tag) extends Table[Photo](tag, "photos") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def description = column[String]("description")
    def source = column[String]("source")
    def creator_id = column[Long]("creator_id")
    def created_at = column[Instant]("created_at")
    def updated_at = column[Instant]("updated_at")

    def * = (
      id,
      title,
      description,
      source,
      creator_id,
      created_at,
      updated_at
    ) <> ((Photo.apply _).tupled, Photo.unapply)
  }

  val photos = TableQuery[PhotosTableDef]
}
