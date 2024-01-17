package com.authService.models

import com.authService.utils.Profile
import play.api.libs.json._
import slick.jdbc.JdbcProfile

case class User(id: Long, email: String, password: String)
object User {
  implicit val format: Format[User] = Json.format[User]
}

trait UsersTable { this: Profile =>
  import profile.api._

  class UsersTableDef(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email")
    private def password = column[String]("password")

    def * = (id, email, password) <> ((User.apply _).tupled, User.unapply)
  }

  val users = TableQuery[UsersTableDef]
}
