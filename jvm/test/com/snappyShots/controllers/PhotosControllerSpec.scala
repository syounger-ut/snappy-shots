package com.snappyShots.controllers

import com.amazonaws.services.s3.model.PutObjectResult
import com.snappyShots.UnitSpec
import com.snappyShots.auth.{AuthAction, AuthService}
import com.snappyShots.models.Photo
import com.snappyShots.repositories.PhotoRepository
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.Json
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import java.time.Instant
import scala.util.{Failure, Success, Try}
import scala.concurrent.Future

class PhotosControllerSpec extends UnitSpec {
  import scala.concurrent.ExecutionContext.Implicits.global

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
      id = mockPhotoId,
      title = "My wonderful photo",
      description = Some("A beautiful photo scenery"),
      source = Some("https://www.example.com/my-photo.jpg"),
      creatorId = mockUserId,
      createdAt = Some(mockDateTime),
      updatedAt = Some(mockDateTime)
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
          "creatorId":${mockUserId}
        }"""

        val response = setupResponse(Some(requestBody))
        val responseStatus = status(response)
        val bodyText: String = contentAsString(response)
        assert(responseStatus == CREATED)
        bodyText contains s"{\"id\":${mockPhotoId},\"title\":\"My wonderful photo\",\"description\":\"A beautiful photo scenery\",\"source\":\"https://www.example.com/my-photo.jpg\",\"creatorId\":${mockUserId}}"
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
          bodyText == s"{\"photos\":[{\"id\":${mockPhotoId},\"title\":\"My wonderful photo\",\"description\":\"A beautiful photo scenery\",\"source\":\"https://www.example.com/my-photo.jpg\",\"creatorId\":${mockUserId},\"createdAt\":\"${mockDateTime.toString}\",\"updatedAt\":\"${mockDateTime.toString}\"}]}"
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
          bodyText == s"{\"id\":${mockPhotoId},\"title\":\"My wonderful photo\",\"description\":\"A beautiful photo scenery\",\"source\":\"https://www.example.com/my-photo.jpg\",\"creatorId\":${mockUserId},\"createdAt\":\"${mockDateTime.toString}\",\"updatedAt\":\"${mockDateTime.toString}\"}"
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
      creatorId = mockUserId
    )

    val mockPhotoUpdateResponse = mockPhotoUpdate.copy(
      createdAt = Some(mockDateTime),
      updatedAt = Some(mockDateTime)
    )

    val requestBodyGood = s"""{
      "id":${mockPhotoId},
      "title":"Updated title",
      "description":"A beautiful photo scenery",
      "source":"https://www.example.com/my-photo.jpg",
      "creatorId":${mockUserId}
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
            bodyText == s"{\"id\":${mockPhotoId},\"title\":\"Updated title\",\"description\":\"A beautiful photo scenery\",\"source\":\"https://www.example.com/my-photo.jpg\",\"creatorId\":${mockUserId},\"createdAt\":\"${mockDateTime.toString}\",\"updatedAt\":\"${mockDateTime.toString}\"}"
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

  describe("#uploadPhotoObject") {
    val mockRequest = mock[Request[MultipartFormData[TemporaryFile]]]

    def setupMockRequest(formData: MultipartFormData[TemporaryFile]): Unit = {
      (mockRequest.headers _).expects().returns(Headers(authHeaders))
      (mockRequest.body _).expects().returns(formData)
    }

    def mockUploadObject(returnValue: Try[PutObjectResult]): Unit = {
      (mockPhotoRepository.uploadObject _)
        .expects(*, *, *, *)
        .returns(Future(returnValue))
    }

    def setupResponse(fileName: String) = {
      setupAuth()

      val tempFile = play.api.libs.Files.SingletonTemporaryFileCreator
        .create("mock-photo", "txt")
      val mockFile =
        FilePart(fileName, "mock-file", Option("text/plain"), tempFile)
      val formData = new MultipartFormData(
        dataParts = Map("" -> Seq("mock_data")),
        files = Seq(mockFile),
        badParts = Seq()
      )

      setupMockRequest(formData)

      controller
        .uploadPhotoObject(1)
        .apply(mockRequest)
    }

    describe("when a valid file is provided") {
      describe("when the file upload succeeds") {
        it("should return Ok") {
          val mockObjectResult = Success(new PutObjectResult())
          mockUploadObject(mockObjectResult)

          val response = setupResponse("file")
          assert(status(response) == OK)
          val bodyText: String = contentAsString(response)
          assert(bodyText == """{"message":"File uploaded"}""")
        }
      }

      describe("when the file upload fails") {
        it("should return Bad Request") {
          val mockObjectResult = Failure(new Exception("mock-error"))
          mockUploadObject(mockObjectResult)

          val response = setupResponse("file")
          assert(status(response) == BAD_REQUEST)
          val bodyText: String = contentAsString(response)
          assert(bodyText == """{"message":"mock-error"}""")
        }
      }

      describe("when the file upload errors") {
        it("should return Bad Request") {
          (mockPhotoRepository.uploadObject _)
            .expects(*, *, *, *)
            .returns(Future.failed(new Exception("mock-error")))

          val response = setupResponse("file")
          assert(status(response) == BAD_REQUEST)
          val bodyText: String = contentAsString(response)
          assert(bodyText == """{"message":"mock-error"}""")
        }
      }
    }

    describe("when a valid file is not provided") {
      it("should return Bad Request") {
        val response = setupResponse("not-recognised-file-name")
        assert(status(response) == BAD_REQUEST)
        val bodyText: String = contentAsString(response)
        assert(bodyText == """{"message":"No file found"}""")
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

  describe("#deletePhotoObject") {
    def setupResponse(fileName: String) = {
      setupAuth()

      val request = FakeRequest().withHeaders(authHeaders)
      controller
        .deletePhotoObject(mockPhotoId)
        .apply(request)
    }

    def mockDeleteObject(): Unit = {
      (mockPhotoRepository.deleteObject _)
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
        (mockPhotoRepository.deleteObject _)
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
