package com.authService.controllers

import com.authService.UnitSpec
import com.authService.auth.{AuthAction, AuthService}
import com.authService.models.Photo
import com.authService.repositories.{PhotoRepository, UserRepository}
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import java.time.Instant
import scala.util.Success
import scala.concurrent.Future

class PhotosControllerSpec extends UnitSpec {
  val mockPhotoRepository: PhotoRepository = mock[PhotoRepository]
  val controllerComponents: ControllerComponents =
    Helpers.stubControllerComponents()
  val mockAuthService: AuthService = mock[AuthService]
  val mockAuthAction: AuthAction =
    new AuthAction(mock[BodyParsers.Default], mockAuthService)(
      controllerComponents.executionContext
    )
  val controller = new PhotosController(
    controllerComponents,
    mockPhotoRepository,
    mockAuthAction
  )(controllerComponents.executionContext)

  val mockJwtToken = "mock-auth-token"
  val authHeaders = ("Authorization", s"Bearer ${mockJwtToken}")

  def setupAuth(): Unit = {
    (mockAuthService.validateToken _)
      .expects(mockJwtToken)
      .returns(Success("mock-header", "mock-claim", "mock-signature"))
  }

  describe("#getPhoto") {
    val mockDateTime = Instant.now()
    val mockPhoto = Some(
      Photo(
        1,
        "My wonderful photo",
        Some("A beautiful photo scenery"),
        Some("https://www.example.com/my-photo.jpg"),
        1,
        Some(mockDateTime),
        Some(mockDateTime)
      )
    )
    val mockId: Long = 123

    def setupPhotoRepository(mockResponse: Option[Photo] = None) = {
      mockResponse match {
        case Some(res) =>
          (mockPhotoRepository.get _)
            .expects(mockId)
            .returns(Future.successful(Some(res)))
        case None =>
          (mockPhotoRepository.get _)
            .expects(mockId)
            .returns(Future.successful(None))
      }
    }

    def setupResponse(returnPhoto: Boolean) = {
      setupAuth()
      if (returnPhoto) {
        setupPhotoRepository(mockPhoto)
      } else {
        setupPhotoRepository()
      }
      controller.getPhoto(123).apply(FakeRequest().withHeaders(authHeaders))
    }

    describe("when a photo exists") {
      it("should return the photo") {
        val response = setupResponse(true)
        val responseStatus = status(response)
        val bodyText: String = contentAsString(response)
        assert(responseStatus == OK)
        assert(
          bodyText == s"{\"id\":1,\"title\":\"My wonderful photo\",\"description\":\"A beautiful photo scenery\",\"source\":\"https://www.example.com/my-photo.jpg\",\"creator_id\":1,\"created_at\":\"${mockDateTime.toString}\",\"updated_at\":\"${mockDateTime.toString}\"}"
        )
      }
    }

    describe("when photo do not exist") {
      it("should return not found status") {
        val response = setupResponse(false)
        val responseStatus = status(response)
        assert(responseStatus == NOT_FOUND)
      }
    }
  }
}
