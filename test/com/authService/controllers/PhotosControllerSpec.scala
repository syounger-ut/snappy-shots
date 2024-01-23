package com.authService.controllers

import com.authService.UnitSpec
import com.authService.auth.{AuthAction, AuthService}
import com.authService.models.Photo
import com.authService.repositories.PhotoRepository
import play.api.libs.json.Json
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
  val authHeaders: (String, String) =
    ("Authorization", s"Bearer ${mockJwtToken}")

  val mockId: Int = 123
  val mockDateTime: Instant = Instant.now()
  val mockPhoto: Option[Photo] = Some(
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

  def setupAuth(): Unit = {
    (mockAuthService.validateToken _)
      .expects(mockJwtToken)
      .returns(Success("mock-header", "mock-claim", "mock-signature"))
  }

  describe("#getPhotos") {
    def setupPhotoRepository(mockResponse: Option[Photo] = None) = {
      mockResponse match {
        case Some(res) =>
          (mockPhotoRepository.list _)
            .expects()
            .returns(Future.successful(List(res)))
        case None =>
          (mockPhotoRepository.list _)
            .expects()
            .returns(Future.successful(List()))
      }
    }

    def setupResponse(returnPhoto: Boolean) = {
      setupAuth()
      if (returnPhoto) {
        setupPhotoRepository(mockPhoto)
      } else {
        setupPhotoRepository()
      }
      controller.getPhotos().apply(FakeRequest().withHeaders(authHeaders))
    }

    describe("when a photo exists") {
      it("should return the photo") {
        val response = setupResponse(true)
        val responseStatus = status(response)
        val bodyText: String = contentAsString(response)
        assert(responseStatus == OK)
        assert(
          bodyText == s"[{\"id\":1,\"title\":\"My wonderful photo\",\"description\":\"A beautiful photo scenery\",\"source\":\"https://www.example.com/my-photo.jpg\",\"creator_id\":1,\"created_at\":\"${mockDateTime.toString}\",\"updated_at\":\"${mockDateTime.toString}\"}]"
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

  describe("#getPhoto") {
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

  describe("#updatePhoto") {
    val mockPhotoUpdate = Photo(
      id = 1,
      title = "Updated title",
      description = Some("A beautiful photo scenery"),
      source = Some("https://www.example.com/my-photo.jpg"),
      creator_id = 1,
      created_at = None,
      updated_at = None
    )

    val mockPhotoUpdateResponse = mockPhotoUpdate.copy(
      created_at = Some(mockDateTime),
      updated_at = Some(mockDateTime)
    )

    val requestBodyGood = s"""{
      "id":1,
      "title":"Updated title",
      "description":"A beautiful photo scenery",
      "source":"https://www.example.com/my-photo.jpg",
      "creator_id":1
    }"""
    val requestBodyBad = s"""{
      "title":"Missing required fields"
    }"""

    def setupPhotoRepository(
      repositoryCallCount: Int,
      mockPhotoToUpdate: Photo,
      mockResponse: Option[Photo] = None
    ) = {
      mockResponse match {
        case Some(res) =>
          (mockPhotoRepository.update _)
            .expects(mockId, mockPhotoToUpdate)
            .returns(Future.successful(Some(res)))
            .repeated(repositoryCallCount)
        case None =>
          (mockPhotoRepository.update _)
            .expects(mockId, mockPhotoToUpdate)
            .returns(Future.successful(None))
            .repeated(repositoryCallCount)
      }
    }

    def setupResponse(
      returnPhoto: Boolean,
      requestBody: String,
      repositoryCallCount: Int
    ) = {
      setupAuth()
      if (returnPhoto) {
        setupPhotoRepository(
          repositoryCallCount,
          mockPhotoUpdate,
          Some(mockPhotoUpdateResponse)
        )
      } else {
        setupPhotoRepository(repositoryCallCount, mockPhotoUpdate)
      }

      val fakeRequest = FakeRequest()
        .withHeaders(authHeaders)
        .withJsonBody(Json.parse(requestBody))

      controller.updatePhoto(mockId).apply(fakeRequest)
    }

    describe("when the json payload is not valid") {
      it("should return bad request status") {
        val response = setupResponse(
          repositoryCallCount = 0,
          returnPhoto = false,
          requestBody = requestBodyBad
        )
        val responseStatus = status(response)
        assert(responseStatus == BAD_REQUEST)
      }
    }

    describe("when the json payload is valid") {
      describe("when the photo exists") {
        it("should return the photo") {
          val response = setupResponse(
            repositoryCallCount = 1,
            returnPhoto = true,
            requestBody = requestBodyGood
          )
          val responseStatus = status(response)
          val bodyText: String = contentAsString(response)
          assert(responseStatus == OK)
          assert(
            bodyText == s"{\"id\":1,\"title\":\"Updated title\",\"description\":\"A beautiful photo scenery\",\"source\":\"https://www.example.com/my-photo.jpg\",\"creator_id\":1,\"created_at\":\"${mockDateTime.toString}\",\"updated_at\":\"${mockDateTime.toString}\"}"
          )
        }
      }

      describe("when the photo doesn't exist") {
        it("should return bad request status") {
          val response = setupResponse(
            repositoryCallCount = 1,
            returnPhoto = false,
            requestBody = requestBodyGood
          )
          val responseStatus = status(response)
          assert(responseStatus == NOT_FOUND)
        }
      }
    }
  }

  describe("#deletePhoto") {
    def setupPhotoRepository(mockResponse: Int) = {
      (mockPhotoRepository.delete _)
        .expects(mockId)
        .returns(Future.successful(mockResponse))
    }

    def setupResponse(repositoryResponse: Int) = {
      setupAuth()
      setupPhotoRepository(repositoryResponse)

      controller
        .deletePhoto(mockId)
        .apply(FakeRequest().withHeaders(authHeaders))
    }

    describe("when the photo exists") {
      it("should return ok status") {
        val response = setupResponse(repositoryResponse = 1)
        val responseStatus = status(response)
        assert(responseStatus == OK)
      }
    }

    describe("when the photo doesn't exist") {
      it("should return not found status") {
        val response = setupResponse(repositoryResponse = 0)
        val responseStatus = status(response)
        assert(responseStatus == NOT_FOUND)
      }
    }
  }
}
