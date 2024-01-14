package com.authService.repositories

import com.authService.models._
import com.authService.utils.{Connection, SlickDBDriver}
import com.google.inject.Inject
import org.postgresql.util.PSQLException
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.util.{Failure, Success}

class UserRepository @Inject()(override val profile: JdbcProfile = SlickDBDriver.getDriver) extends UsersTable with Profile {
  import scala.concurrent.ExecutionContext.Implicits.global

  import profile.api._

  val db = new Connection(profile).db()

  def addUser(user: User): Future[User] = {
    val insertQuery = users returning users.map(_.id) into ((user, id) => user.copy(id = id))
    val action = insertQuery += user

    db.run(action.asTry).map {
      case Failure(exception: PSQLException) =>
        throw new IllegalStateException(exception.getMessage)
      case Success(user: User) => {
        user
      }
    }
  }

  def getUser(id: Long): Future[Option[User]] = {
    val query = users.filter(_.id === id).result.headOption
    db.run(query)
  }

  def findUser(email: String): Future[Option[User]] = {
    val query = users.filter(_.email === email).result.headOption
    db.run(query)
  }
}
