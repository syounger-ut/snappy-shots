package com.authService.controllers

import com.authService.UnitSpec
import com.authService.auth.{AuthAction, AuthService}
import com.authService.models.{Comment, Post, User}
import com.authService.repositories.{DataRepository, UserRepository}
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.util.Success

class ApiControllerSpec extends UnitSpec {
  val mockDataRepository: DataRepository = mock[DataRepository]
  val controllerComponents: ControllerComponents =
    Helpers.stubControllerComponents()
  val mockAuthService: AuthService = mock[AuthService]
  val mockUserRepository: UserRepository = mock[UserRepository]
  val mockAuthAction: AuthAction =
    new AuthAction(mock[BodyParsers.Default], mockAuthService)(
      controllerComponents.executionContext
    )
  val controller = new ApiController(
    controllerComponents,
    mockDataRepository,
    mockUserRepository,
    mockAuthAction
  )(controllerComponents.executionContext)

  describe("#ping") {
    it("should return ok") {
      val response = controller.ping().apply(FakeRequest())
      val bodyText: String = contentAsString(response)
      assert(bodyText == """Hello, Scala!""")
    }
  }

  describe("authenticated routes") {
    val mockJwtToken = "mock-auth-token"
    val authHeaders = ("Authorization", s"Bearer ${mockJwtToken}")

    def setupAuth(): Unit = {
      (mockAuthService.validateToken _)
        .expects(mockJwtToken)
        .returns(Success("mock-header", s"""{"user_id":1}""", "mock-signature"))
    }

    describe("#getPost") {
      val mockPost = Some(Post(1, "something"))

      def setupDataRepository(mockResponse: Option[Post] = None) = {
        mockResponse match {
          case Some(res) =>
            (mockDataRepository.getPost _).expects(123).returns(Some(res))
          case None => (mockDataRepository.getPost _).expects(123).returns(None)
        }
      }

      def setupResponse(returnPost: Boolean) = {
        setupAuth()
        if (returnPost) {
          setupDataRepository(mockPost)
        } else {
          setupDataRepository()
        }
        controller.getPost(123).apply(FakeRequest().withHeaders(authHeaders))
      }

      describe("when posts exist") {
        it("should return posts") {
          val response = setupResponse(true)
          val responseStatus = status(response)
          val bodyText: String = contentAsString(response)
          assert(responseStatus == OK)
          assert(bodyText == "{\"id\":1,\"content\":\"something\"}")
        }
      }

      describe("when posts do not exist") {
        it("should return not found status") {
          val response = setupResponse(false)
          val responseStatus = status(response)
          assert(responseStatus == NOT_FOUND)
        }
      }
    }

    describe("#getComments") {
      val mockComments = Seq(Comment(1, 2, "A comment", "Foo Bar"))

      def setupDataRepository(mockResponse: Seq[Comment]) = {
        (mockDataRepository.getComments _)
          .expects(123)
          .returns(mockResponse)
      }

      def setupResponse() = {
        setupAuth()
        setupDataRepository(mockComments)
        controller
          .getComments(123)
          .apply(FakeRequest().withHeaders(authHeaders))
      }

      it("should return comments") {
        val response = setupResponse()
        val responseStatus = status(response)
        val bodyText: String = contentAsString(response)
        assert(responseStatus == OK)
        assert(
          bodyText == "[{\"id\":1,\"postId\":2,\"text\":\"A comment\",\"authorName\":\"Foo Bar\"}]"
        )
      }
    }

    describe("#addUser") {
      val mockUser = User(0, email = "john@email.com", password = "foobar")
      val mockReturnUser =
        User(1, email = "john@email.com", password = "foobar")

      def setupUserRepository() = {
        (mockUserRepository.addUser _)
          .expects(mockUser)
          .returns(Future.successful(mockReturnUser))
      }

      def setupResponse() = {
        setupAuth()
        setupUserRepository()
        controller
          .addUser()
          .apply(FakeRequest().withHeaders(authHeaders))
      }

      it("should return comments") {
        val response = setupResponse()
        val responseStatus = status(response)
        val bodyText: String = contentAsString(response)
        assert(responseStatus == OK)
        assert(
          bodyText == "{\"id\":1,\"email\":\"john@email.com\",\"password\":\"foobar\"}"
        )
      }
    }
  }
}
