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

  val mockUserId: Int = 321
  val mockPhotoId: Int = 123
  val mockDateTime: Instant = Instant.now()
  val mockPhoto: Option[Photo] = Some(
    Photo(
      mockPhotoId,
      "My wonderful photo",
      Some("A beautiful photo scenery"),
      Some("https://www.example.com/my-photo.jpg"),
      mockUserId,
      Some(mockDateTime),
      Some(mockDateTime)
    )
  )

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

  describe("#createPhoto") {
    def setupResponse(requestBody: Option[String]) = {
      setupAuth()

      requestBody match {
        case Some(body) =>
          controller
            .createPhoto()
            .apply(
              FakeRequest()
                .withHeaders(authHeaders)
                .withJsonBody(
                  Json.parse(body)
                )
            )
        case None =>
          controller
            .createPhoto()
            .apply(
              FakeRequest()
                .withHeaders(authHeaders)
            )
      }
    }

    describe("when the json payload is valid") {
      it("should create a photo") {
        (mockPhotoRepository.create _)
          .expects(*)
          .returns(Future.successful(mockPhoto.get))

        val requestBody = s"""{
          "id":123,
          "title":"My wonderful photo",
          "description":"A beautiful photo scenery",
          "source":"https://www.example.com/my-photo.jpg",
          "creator_id":${mockUserId}
        }"""

        val response = setupResponse(Some(requestBody))
        val responseStatus = status(response)
        val bodyText: String = contentAsString(response)
        assert(responseStatus == CREATED)
        bodyText contains s"{\"id\":${mockPhotoId},\"title\":\"My wonderful photo\",\"description\":\"A beautiful photo scenery\",\"source\":\"https://www.example.com/my-photo.jpg\",\"creator_id\":${mockUserId}}"
      }
    }

    describe("when the json payload is invalid") {
      it("should return Bad Request") {
        val requestBody = s"""{
          "foo": "Some invalid payload"
        }"""

        val response = setupResponse(Some(requestBody))
        val responseStatus = status(response)
        val bodyText: String = contentAsString(response)
        assert(responseStatus == BAD_REQUEST)
        assert(bodyText == s"{\"message\":\"Invalid photo\"}")
      }
    }

    describe("when no json payload is provided") {
      it("should return Bad Request") {
        val response = setupResponse(None)
        val responseStatus = status(response)
        val bodyText: String = contentAsString(response)
        assert(responseStatus == BAD_REQUEST)
        assert(
          bodyText == s"{\"message\":\"Invalid photo, no payload was provided\"}"
        )
      }
    }
  }

  describe("#getPhotos") {
    def setupPhotoRepository(mockResponse: Option[Photo] = None) = {
      mockResponse match {
        case Some(res) =>
          (mockPhotoRepository.list _)
            .expects(mockUserId)
            .returns(Future.successful(List(res)))
        case None =>
          (mockPhotoRepository.list _)
            .expects(mockUserId)
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
          bodyText == s"{\"photos\":[{\"id\":${mockPhotoId},\"title\":\"My wonderful photo\",\"description\":\"A beautiful photo scenery\",\"source\":\"https://www.example.com/my-photo.jpg\",\"creator_id\":${mockUserId},\"created_at\":\"${mockDateTime.toString}\",\"updated_at\":\"${mockDateTime.toString}\"}]}"
        )
      }
    }

    describe("when photo do not exist") {
      it("should return not found status") {
        val response = setupResponse(false)
        val responseStatus = status(response)
        assert(responseStatus == NOT_FOUND)
        val bodyText: String = contentAsString(response)
        assert(bodyText == s"{\"message\":\"Photos not found\",\"photos\":[]}")
      }
    }
  }

  describe("#getPhoto") {
    def setupPhotoRepository(mockResponse: Option[Photo] = None) = {
      mockResponse match {
        case Some(res) =>
          (mockPhotoRepository.get _)
            .expects(mockPhotoId, mockUserId)
            .returns(Future.successful(Some(res)))
        case None =>
          (mockPhotoRepository.get _)
            .expects(mockPhotoId, mockUserId)
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
          bodyText == s"{\"id\":${mockPhotoId},\"title\":\"My wonderful photo\",\"description\":\"A beautiful photo scenery\",\"source\":\"https://www.example.com/my-photo.jpg\",\"creator_id\":${mockUserId},\"created_at\":\"${mockDateTime.toString}\",\"updated_at\":\"${mockDateTime.toString}\"}"
        )
      }
    }

    describe("when photo do not exist") {
      it("should return not found status") {
        val response = setupResponse(false)
        val responseStatus = status(response)
        assert(responseStatus == NOT_FOUND)
        val bodyText: String = contentAsString(response)
        assert(bodyText == s"{\"message\":\"Photo not found\"}")
      }
    }
  }

  describe("#updatePhoto") {
    val mockPhotoUpdate = Photo(
      id = mockPhotoId,
      title = "Updated title",
      description = Some("A beautiful photo scenery"),
      source = Some("https://www.example.com/my-photo.jpg"),
      creator_id = mockUserId
    )

    val mockPhotoUpdateResponse = mockPhotoUpdate.copy(
      created_at = Some(mockDateTime),
      updated_at = Some(mockDateTime)
    )

    val requestBodyGood = s"""{
      "id":${mockPhotoId},
      "title":"Updated title",
      "description":"A beautiful photo scenery",
      "source":"https://www.example.com/my-photo.jpg",
      "creator_id":${mockUserId}
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
            .expects(mockPhotoId, mockUserId, mockPhotoToUpdate)
            .returns(Future.successful(Some(res)))
            .repeated(repositoryCallCount)
        case None =>
          (mockPhotoRepository.update _)
            .expects(mockPhotoId, mockUserId, mockPhotoToUpdate)
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

      controller.updatePhoto(mockPhotoId).apply(fakeRequest)
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
            bodyText == s"{\"id\":${mockPhotoId},\"title\":\"Updated title\",\"description\":\"A beautiful photo scenery\",\"source\":\"https://www.example.com/my-photo.jpg\",\"creator_id\":${mockUserId},\"created_at\":\"${mockDateTime.toString}\",\"updated_at\":\"${mockDateTime.toString}\"}"
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
          val bodyText: String = contentAsString(response)
          assert(bodyText == s"{\"message\":\"Photo not updated\"}")
        }
      }
    }
  }

  describe("#deletePhoto") {
    def setupPhotoRepository(mockResponse: Int) = {
      (mockPhotoRepository.delete _)
        .expects(mockPhotoId, mockUserId)
        .returns(Future.successful(mockResponse))
    }

    def setupResponse(repositoryResponse: Int) = {
      setupAuth()
      setupPhotoRepository(repositoryResponse)

      controller
        .deletePhoto(mockPhotoId)
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
        val bodyText: String = contentAsString(response)
        assert(bodyText == s"{\"message\":\"Photo not deleted\"}")
      }
    }
  }
}
