package com.authService.auth

import com.authService.AsyncUnitSpec
import play.api.http.Status._
import play.api.mvc.{BodyParsers, Headers}
import play.api.mvc.Results.Ok
import play.api.test.{FakeRequest, Helpers}

import scala.util.{Failure, Success, Try}

class AuthActionSpec extends AsyncUnitSpec {
  val mockAuthService: AuthService = mock[AuthService]
  val mockJwtToken = "mock-auth-token"
  val mockHeaders: Headers = Headers(
    "Authorization" -> s"Bearer ${mockJwtToken}"
  )

  def authAction: AuthAction = {
    val controllerComponents = Helpers.stubControllerComponents()
    val mockBodyParsers = mock[BodyParsers.Default]

    new AuthAction(mockBodyParsers, mockAuthService)(
      controllerComponents.executionContext
    )
  }

  def setupAuthServiceMock(
    mockResponse: Try[(String, String, String)]
  ): Unit = {
    (mockAuthService.validateToken _)
      .expects(mockJwtToken)
      .returns(mockResponse)
  }

  describe("when no auth token is provided") {
    it("should should be unauthorized") {
      val subject = authAction { Ok }.apply(FakeRequest())
      subject map { r => assert(r.header.status == UNAUTHORIZED) }
    }
  }

  describe("when an auth token is provided") {
    describe("when the token is valid") {
      describe("when the jwt claim payload is invalid") {
        val mockAuthServiceResponse =
          Success("mock-header", s"""{}""", "mock-signature")

        it("should return unauthorized status") {
          setupAuthServiceMock(mockAuthServiceResponse)
          val subject =
            authAction { Ok }.apply(FakeRequest().withHeaders(mockHeaders))
          subject map { r => assert(r.header.status == UNAUTHORIZED) }
        }
      }

      describe("when jwt claim payload is valid") {
        val mockAuthServiceResponse =
          Success("mock-header", s"""{"user_id":1}""", "mock-signature")

        it("should return ok status") {
          setupAuthServiceMock(mockAuthServiceResponse)

          val subject = authAction { request =>
            assert(request.userId == 1)
            Ok
          }.apply(FakeRequest().withHeaders(mockHeaders))

          subject map { r =>
            assert(r.header.status == OK)
          }
        }
      }
    }

    describe("when the token is invalid") {
      val headers = Headers("Authorization" -> s"Bearer ${mockJwtToken}")

      it("should return unauthorized status") {
        setupAuthServiceMock(Failure(new Exception("Something went wrong")))
        val subject =
          authAction { Ok }.apply(FakeRequest().withHeaders(headers))
        subject map { r => assert(r.header.status == UNAUTHORIZED) }
      }
    }
  }
}
