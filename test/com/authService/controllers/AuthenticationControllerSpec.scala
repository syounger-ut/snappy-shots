package com.authService.controllers

import com.authService.UnitSpec
import com.authService.auth.AuthService
import com.authService.models.User
import com.authService.repositories.UserRepository
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test._
import play.api.test.Helpers._
import com.github.t3hnar.bcrypt._

import scala.concurrent.Future

class AuthenticationControllerSpec extends UnitSpec {
  import scala.concurrent.ExecutionContext.Implicits.global

  val mockAuthService: AuthService = mock[AuthService]
  val mockUserRepository: UserRepository = mock[UserRepository]
  val mockUserId = 123
  val mockEmail = "foo@bar.com"
  val mockPassword = "password"

  def setAuthServiceValues(userId: Long): Unit = {
    (mockAuthService.createToken _).expects(userId).returning("fake-token")
  }

  describe("#login") {
    def setFindUserValue(user: Option[User]): Unit = {
      (mockUserRepository.findUser _)
        .expects(mockEmail)
        .returning(Future(user))
    }

    def makeRequest(): Future[Result] = {
      val controller = new AuthenticationController(
        Helpers.stubControllerComponents(),
        mockAuthService,
        mockUserRepository
      )
      val request = FakeRequest.apply().withJsonBody(Json.parse(s"""{"email":"${mockEmail}", "password":"${mockPassword}"}"""))
      controller.login().apply(request)
    }

    describe("when a user is found") {
      def setupMocks(): Unit = {
        setAuthServiceValues(mockUserId)
        setFindUserValue(Some(User(mockUserId, mockEmail, mockPassword.bcryptSafeBounded.get)))
      }

      it("should return an authentication token") {
        setupMocks()
        val response = makeRequest()

        assert(status(response) == 200)
        val bodyText: String = contentAsString(response)
        assert(bodyText ==
          """{"token":"fake-token","user":{"id":"123","email":"foo@bar.com"},"message":"Valid credentials"}"""
        )
      }
    }

    describe("when a user is not found") {
      def setupMocks(): Unit = {
        setFindUserValue(None)
      }

      it("should return a 404") {
        setupMocks()
        val response = makeRequest()

        assert(status(response) == 404)
        val bodyText: String = contentAsString(response)
        assert(bodyText ==
          """{"message":"User not found"}"""
        )
      }
    }

    describe("when a user is found but the password is incorrect") {
      def setupMocks(): Unit = {
        setFindUserValue(Some(User(mockUserId, mockEmail, "wrong-password".bcryptSafeBounded.get)))
      }

      it("should return a 401") {
      setupMocks()
      val response = makeRequest()

      assert(status(response) == 401)
        val bodyText: String = contentAsString(response)
        assert(bodyText ==
          """{"message":"Invalid credentials"}"""
        )
      }
    }

    describe("when the password is not a valid bcrypt hash") {
      def setupMocks(): Unit = {
        setFindUserValue(Some(User(mockUserId, mockEmail, "not-a-bcrypt-hash")))
      }

      it("should return a 401") {
        setupMocks()
        val response = makeRequest()

        assert(status(response) == 401)
        val bodyText: String = contentAsString(response)
        assert(bodyText ==
          """{"message":"Invalid salt version"}"""
        )
      }
    }
  }

  describe("#register") {
    def setAddUserValue(user: User): Unit = {
      (mockUserRepository.addUser _)
        .expects(*)
        .returning(Future(user))
    }

    def makeRequest(): Future[Result] = {
      val controller = new AuthenticationController(
        Helpers.stubControllerComponents(),
        mockAuthService,
        mockUserRepository
      )
      val request = FakeRequest().withJsonBody(Json.parse(s"""{"email":"${mockEmail}", "password":"${mockPassword}"}"""))
      controller.register().apply(request)
    }

    describe("when the user creation is successful") {
      it("should return 200") {
        setAddUserValue(User(mockUserId, mockEmail, mockPassword))
        setAuthServiceValues(mockUserId)
        val response = makeRequest()

        assert(status(response) == 200)
        val bodyText: String = contentAsString(response)
        assert(bodyText ==
          """{"token":"fake-token","user":{"id":"123","email":"foo@bar.com"},"message":"User created successfully"}"""
        )
      }
    }

    describe("when something went wrong with user creation") {
      it("should return 400") {
        (mockUserRepository.addUser _)
          .expects(*)
          .returning(Future.failed(new Exception("Something went wrong")))
        val response = makeRequest()

        assert(status(response) == 400)
        val bodyText: String = contentAsString(response)
        assert(bodyText == """{"message":"Something went wrong"}""")
      }
    }
  }
}
