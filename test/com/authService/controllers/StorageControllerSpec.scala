package com.authService.controllers

import com.authService.UnitSpec
import com.authService.auth.{AuthAction, AuthService}
import com.authService.repositories.StorageRepository
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.util.Success

class StorageControllerSpec extends UnitSpec {
  import scala.concurrent.ExecutionContext.Implicits.global

  val controllerComponents: ControllerComponents =
    Helpers.stubControllerComponents()
  val mockStorageRepository: StorageRepository = mock[StorageRepository]
  val mockAuthService: AuthService = mock[AuthService]
  val mockAuthAction: AuthAction =
    new AuthAction(mock[BodyParsers.Default], mockAuthService)(
      controllerComponents.executionContext
    )

  val mockJwtToken = "mock-auth-token"
  val authHeaders: (String, String) =
    ("Authorization", s"Bearer ${mockJwtToken}")

  val controller = new StorageController(
    controllerComponents,
    mockStorageRepository,
    mockAuthAction
  )(controllerComponents.executionContext)

  val mockUserId = 123

  def setupAuth(): Unit = {
    (mockAuthService.validateToken _)
      .expects(mockJwtToken)
      .returns(
        Success(
          "mock-header",
          s"""{"user_id":${mockUserId}}""",
          "mock-signature"
        )
      )
  }

  describe("#delete") {
    def setupResponse(fileName: String) = {
      setupAuth()

      val request = FakeRequest().withHeaders(authHeaders)
      controller
        .delete(fileName)
        .apply(request)
    }

    def mockDeleteObject(): Unit = {
      (mockStorageRepository.deleteObject _)
        .expects(*, *)
        .returns(Future(()))
    }

    describe("when the file is deleted") {
      it("should return Ok") {
        mockDeleteObject()

        val response = setupResponse("file")
        assert(status(response) == OK)
        val bodyText: String = contentAsString(response)
        assert(bodyText == """{"message":"File deleted"}""")
      }
    }

    describe("when the file is not deleted") {
      it("should return Bad Request") {
        (mockStorageRepository.deleteObject _)
          .expects(*, *)
          .returns(Future.failed(new Exception("mock-error")))

        val response = setupResponse("file")
        assert(status(response) == BAD_REQUEST)
        val bodyText: String = contentAsString(response)
        assert(bodyText == """{"message":"mock-error"}""")
      }
    }
  }
}
