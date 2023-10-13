package com.authService.controllers

import com.authService.UnitSpec
import com.authService.auth.AuthService

import play.api.test._
import play.api.test.Helpers._

class AuthenticationControllerSpec extends UnitSpec {
  val mockAuthService: AuthService = mock[AuthService]

  def setAuthServiceValues(): Unit = {
    (mockAuthService.createToken _).expects().returning("fake-token")
  }

  override def beforeEach(): Unit = {
    setAuthServiceValues()
  }

  it("should return an authentication token") {
    val controller = new AuthenticationController(
      Helpers.stubControllerComponents(),
      mockAuthService
    )
    val response = controller.login().apply(FakeRequest())
    val bodyText: String = contentAsString(response)
    assert(bodyText == """{"token":"fake-token"}""")
  }
}
