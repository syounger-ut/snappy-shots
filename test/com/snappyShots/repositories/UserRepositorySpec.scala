package com.snappyShots.repositories

import com.snappyShots.DbUnitSpec
import com.snappyShots.models.User

class UserRepositorySpec extends DbUnitSpec {
  val repository = new UserRepository

  describe("#addUser") {
    describe("on success") {
      it("should add a user") {
        val subject =
          repository.addUser(User(0, "test_one@test.com", "password"))

        subject
          .map { user =>
            assert(user.id == 1)
            assert(user.email == "test_one@test.com")
          }
      }
    }

    describe("on failure") {
      it("should throw an exception") {
        val longEmail = "123456789123456789123456789123456789123456789123456789"
        val subject = repository.addUser(User(0, longEmail, "password"))
        recoverToExceptionIf[Exception](subject).map { result =>
          result.getMessage should include("Value too long for colum")
        }
      }
    }
  }

  describe("#getUser") {
    it("should return the user") {
      for {
        _ <- repository.addUser(User(0, "test_one@test.com", "password"))
        result <- repository.getUser(1)
      } yield result match {
        case Some(user) => assert(user.email == "test_one@test.com")
      }
    }
  }

  describe("#findUser") {
    it("should return the user") {
      for {
        _ <- repository.addUser(User(0, "test_three@test.com", "password"))
        result <- repository.findUser("test_three@test.com")
      } yield result match {
        case Some(user) => assert(user.email == "test_three@test.com")
      }
    }
  }
}
