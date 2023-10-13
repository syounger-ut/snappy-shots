package com.authService.auth

import com.authService.AsyncUnitSpec
import play.api.http.Status.{OK, UNAUTHORIZED}
import play.api.mvc.{Headers, Result}
import play.api.mvc.Results.Ok
import play.api.test.{FakeRequest, Helpers}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class AuthActionSpec extends AsyncUnitSpec {
  val mockAuthService: AuthService = mock[AuthService]

  def prepareResponse(headers: Headers = Headers()): Future[Result] = {
    val controllerComponents = Helpers.stubControllerComponents()
    val authAction =
      new AuthAction(controllerComponents.parsers.default, mockAuthService)(
        controllerComponents.executionContext
      )
    val request = FakeRequest().withHeaders(headers)
    authAction { _ => Ok }.apply(request)
  }

  describe("when no auth token is provided") {
    it("should should be unauthorized") {
      val subject = prepareResponse()
      subject map { r => assert(r.header.status == UNAUTHORIZED) }
    }
  }

  describe("when an auth token is provided") {
    val mockJwtToken = "mock-auth-token"

    describe("when the token is valid") {
      it("should return ok status") {
        (mockAuthService.validateToken _)
          .expects(mockJwtToken)
          .returns(Success("mock-header", "mock-claim", "mock-signature"))
        val subject =
          prepareResponse(Headers("Authorization" -> s"Bearer ${mockJwtToken}"))
        subject map { r => assert(r.header.status == OK) }
      }
    }

    describe("when the token is invalid") {
      it("should return unauthorized status") {
        (mockAuthService.validateToken _)
          .expects(mockJwtToken)
          .returns(Failure(new Exception("Something went wrong")))
        val subject =
          prepareResponse(Headers("Authorization" -> s"Bearer ${mockJwtToken}"))
        subject map { r => assert(r.header.status == UNAUTHORIZED) }
      }
    }
  }
}
