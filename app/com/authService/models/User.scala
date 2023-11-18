package com.authService.models

import play.api.libs.json._
import slick.jdbc.PostgresProfile

case class User(id: Option[Long], name: String)
object User {
  implicit val format: Format[User] = Json.format[User]
}

class UsersTable(val profile: PostgresProfile) {
  import profile.api._

  class UsersTableDef(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("user_id", O.PrimaryKey, O.AutoInc)
    private def name = column[String]("name")

    def * = (id.?, name) <> ((User.apply _).tupled, User.unapply)
  }

  val users = TableQuery[UsersTableDef]
}

object SlickTables extends UsersTable(PostgresProfile)
