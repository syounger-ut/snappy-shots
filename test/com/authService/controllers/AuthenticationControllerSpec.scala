package com.authService.controllers

import com.authService.UnitSpec
import com.authService.auth.AuthService

import play.api.test._
import play.api.test.Helpers._

class AuthenticationControllerSpec extends UnitSpec {
  it("should do something") {
    val mockAuthService = mock[AuthService]
    (mockAuthService.createToken _).expects().returning("fake-token")

    val controller = new AuthenticationController(Helpers.stubControllerComponents(), mockAuthService)
    val response = controller.login().apply(FakeRequest())
    val bodyText: String = contentAsString(response)
    assert(bodyText == """{"token":"fake-token"}""")
  }
}
