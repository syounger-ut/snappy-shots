package com.authService.repositories

import com.authService.DatabaseUnitSpec
import com.authService.models.User
import org.scalatest.concurrent.ScalaFutures

import java.sql.{Connection, DriverManager}

class UserRepositorySpec extends DatabaseUnitSpec with ScalaFutures {
  val repository = new UserRepository

  describe("#addUser") {
    it("should add a user") {
      val subject = repository.addUser(User(0, "foo@bar.com", "password"))
      subject.map { user =>
        assert(user.id == 1)
        assert(user.email == "foo@bar.com")
      }.recover {
        case e: Exception =>
          println("FAILED TO ADD USER:\n" + e.getMessage)
          fail(e)
      }
    }
  }
}
