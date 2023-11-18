package com.authService.repositories

import com.authService.Connection
import com.authService.models.{SlickTables, _}
import com.google.inject.Inject

import scala.concurrent.Future

class UserRepository @Inject() () {
  import SlickTables.profile.api._

  val db = Connection.db

  def addUser(user: User): Future[User] = {
    val query = SlickTables.users.returning(SlickTables.users) += user
    db.run(query)
  }

  def getUser(id: Long): Future[Option[User]] = {
    val query = SlickTables.users.filter(_.id === id).result.headOption
    db.run(query)
  }
}
