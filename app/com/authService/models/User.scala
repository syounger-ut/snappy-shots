package com.authService.models

import slick.jdbc.PostgresProfile.api._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class User(id: Long, firstName: String, lastName: String, email: String)

class UsersTableDef(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  private def firstName = column[String]("first_name")
  private def lastName = column[String]("last_name")
  private def email = column[String]("email")

  def * = (id, firstName, lastName, email) <>(User.tupled, User.unapply)
}

class Users @Inject() (@NamedDatabase("auth_service") protected val dbConfigProvider: DatabaseConfigProvider)
                      (implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  private val users = TableQuery[UsersTableDef]

  def insert(user: User): Future[Int] = dbConfig.db.run(users += user)
  
  def get(id: Long): Future[Option[User]] = dbConfig.db.run(users.filter(_.id === id).result.headOption)
}
