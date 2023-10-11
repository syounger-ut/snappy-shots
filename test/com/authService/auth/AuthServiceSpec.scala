package com.authService.auth

import com.authService.UnitSpec
import com.authService.mocks.JwtMock
import pdi.jwt.JwtAlgorithm
import pdi.jwt.algorithms.JwtHmacAlgorithm

import scala.util.{Failure, Success}

class AuthServiceSpec extends UnitSpec {
  val mockJwt: JwtMock = mock[JwtMock]

  describe("#createToken") {
    it("should should return a token") {
      (mockJwt.encode (_: String, _: String, _: JwtAlgorithm))
        .expects(*, *, *)
        .returns("fake-jwt-token")

      val svs = new AuthService(mockJwt)
      assert(svs.createToken() == "fake-jwt-token")
    }
  }

  describe("#validateToken") {
    describe("when the token is valid") {
      it("should succeed") {
        (mockJwt.isValid (_: String, _: String, _: Seq[JwtHmacAlgorithm]))
          .expects(*, *, *)
          .returns(true)

        (mockJwt.decodeRawAll (_: String, _: String, _: Seq[JwtHmacAlgorithm]))
          .expects(*, *, *)
          .returns(Success("mock-header", "mock-claim", "mock-signature"))

        val svs = new AuthService(mockJwt)
        assert(svs.validateToken("mock-token") == Success("mock-header", "mock-claim", "mock-signature"))
      }
    }

    describe("when the token is not valid") {
      it("should fail") {
        (mockJwt.isValid(_: String, _: String, _: Seq[JwtHmacAlgorithm]))
          .expects(*, *, *)
          .returns(false)

        val svs = new AuthService(mockJwt)
        assert(svs.validateToken("mock-token").isFailure)
      }
    }
  }
}
