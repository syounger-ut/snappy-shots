package com.authService.repositories

import com.authService.models.{User, Users}
import com.google.inject.Inject

import scala.concurrent.Future

class UserRepository @Inject() (users: Users) {
  def addUser(user: User): Future[Int] = users.insert(user)
  
  def getUser(id: Int): Future[Option[User]] = users.get(id)
}
