package com.authService.repositories

import com.authService.Connection
import com.authService.models.{SlickTables, _}
import com.google.inject.Inject
import org.postgresql.util.PSQLException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class UserRepository @Inject() {
  import SlickTables.profile.api._

  val db = Connection.db

  def addUser(user: User): Future[User] = {
    val query = SlickTables.users.returning(SlickTables.users) += user
    db.run(query.asTry).map {
      case Failure(exception: PSQLException) => throw new IllegalStateException(exception.getMessage)
      case Success(user: User) => user
    }
  }

  def getUser(id: Long): Future[Option[User]] = {
    val query = SlickTables.users.filter(_.id === id).result.headOption
    db.run(query)
  }

  def findUser(email: String): Future[Option[User]] = {
    val query = SlickTables.users.filter(_.email === email).result.headOption
    db.run(query)
  }
}
