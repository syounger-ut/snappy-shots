package com.authService.auth

import com.authService.UnitSpec
import com.authService.mocks.JwtMock
import pdi.jwt.JwtAlgorithm

class AuthServiceSpec extends UnitSpec {
  describe("#createToken") {
    it("should should return a token") {
      val mockJwt = mock[JwtMock]
      (mockJwt.encode (_: String, _: String, _: JwtAlgorithm))
        .expects(*, *, *)
        .returns("fake-jwt-token")

      val svs = new AuthService(mockJwt)
      assert(svs.createToken() == "fake-jwt-token")
    }
  }
}
