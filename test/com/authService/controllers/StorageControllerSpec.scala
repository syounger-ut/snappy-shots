package com.authService.controllers

import com.amazonaws.services.s3.model.PutObjectResult
import com.authService.UnitSpec
import com.authService.auth.{AuthAction, AuthService}
import com.authService.repositories.StorageRepository
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

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
  val mockRequest = mock[Request[MultipartFormData[TemporaryFile]]]

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

  def setupMockRequest(formData: MultipartFormData[TemporaryFile]): Unit = {
    (mockRequest.headers _).expects().returns(Headers(authHeaders))
    (mockRequest.body _).expects().returns(formData)
  }

  def mockUploadObject(returnValue: Try[PutObjectResult]): Unit = {
    (mockStorageRepository.uploadObject _)
      .expects(*, *, *)
      .returns(Future(returnValue))
  }

  describe("#upload") {
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
        .upload()
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
          (mockStorageRepository.uploadObject _)
            .expects(*, *, *)
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
}
