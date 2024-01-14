package com.authService.repositories

import com.authService.AsyncUnitSpec
import com.authService.models.User
import org.scalatest.concurrent.ScalaFutures


class UserRepositorySpec extends AsyncUnitSpec with ScalaFutures {
  val repository = new UserRepository

  describe("#addUser") {
    describe("on success") {
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

    describe("on failure") {
      it("should throw an exception") {
        val longEmail = "123456789123456789123456789123456789123456789123456789"
        val subject = repository.addUser(User(0, longEmail, "password"))
        recoverToExceptionIf[Exception](subject).map { result =>
          result.getMessage should include ("Value too long for colum")
        }
      }
    }
  }

  describe("#getUser") {

  }

  describe("#findUser") {

  }
}
