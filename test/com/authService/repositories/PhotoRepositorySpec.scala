package com.authService.repositories

import com.amazonaws.services.s3.model.PutObjectResult
import com.authService.DbUnitSpec
import com.authService.models.Photo

import java.io.File
import java.net.URL
import java.time.Instant
import scala.concurrent.Future
import scala.util.{Success, Try}

class PhotoRepositorySpec extends DbUnitSpec {
  val mockStorageRepository: StorageRepository = mock[StorageRepository]
  val repository = new PhotoRepository(mockStorageRepository)

  import profile.api._

  val mockUserId = 1
  val mockUserIdTwo = 2
  val mockPhoto: Photo = Photo(
    id = 0,
    title = "My wonderful photo",
    creatorId = mockUserId,
    createdAt = Some(Instant.now()),
    updatedAt = Some(Instant.now())
  )

  val createUserAction =
    sqlu"""INSERT INTO users (id, email, password) VALUES(1, 'foo@bar.com', 'password')"""
  val createSecondUserAction =
    sqlu"""INSERT INTO users (id, email, password) VALUES(2, 'bar@foo.com', 'password')"""
  val createPhotoAction =
    sqlu"""INSERT INTO photos (id, title, creator_id, file_name) VALUES(1, 'My great photo', ${mockUserId}, 'mock-photo-1.jpg')"""
  val createSecondPhotoAction =
    sqlu"""INSERT INTO photos (id, title, creator_id, file_name) VALUES(2, 'My great photo', ${mockUserId}, 'mock-photo-2.jpg')"""
  val createThirdPhotoAction =
    sqlu"""INSERT INTO photos (id, title, creator_id, file_name) VALUES(3, 'My great photo', ${mockUserIdTwo}, 'mock-photo-3')"""
  val createFourthPhotoAction =
    sqlu"""INSERT INTO photos (id, title, creator_id) VALUES(4, 'My great photo', ${mockUserId})"""

  def mockPresignUrl(
    expectedFileName: Option[String],
    returnValue: Option[URL],
    callCount: Int
  ): Unit = {
    expectedFileName match {
      case Some(fileName) =>
        (mockStorageRepository.preSignedUrl _)
          .expects(*, fileName)
          .returning(Future(returnValue.get))
          .repeated(callCount)
      case None =>
        (mockStorageRepository.preSignedUrl _)
          .expects(*, *)
          .returning(Future(returnValue.get))
          .repeated(callCount)
    }
  }

  describe("#create") {
    describe("on success") {
      it("should create a photo") {
        for {
          _ <- db.run(createUserAction)
          photo <- repository.create(mockPhoto)
        } yield photo match {
          case photo =>
            assert(photo.id == 1)
            assert(photo.title == mockPhoto.title)
            assert(photo.createdAt.isDefined)
            assert(photo.updatedAt.isDefined)
        }
      }
    }

    describe("on failure") {
      it("should throw an exception") {
        val createUserAction =
          sqlu"""DELETE FROM users WHERE id = ${mockUserId}"""
        val createUserQuery = db.run(createUserAction)

        val query = for {
          _ <- createUserQuery
          response <- repository.create(mockPhoto)
        } yield response

        recoverToExceptionIf[Exception](query).map { result =>
          result.getMessage should include(
            "Referential integrity constraint violation"
          )
        }
      }
    }
  }

  describe("#get") {
    val mockUrl = new URL("https://example.com")

    def subject(photoId: Int): Future[Option[Photo]] = {
      for {
        _ <- db.run(createUserAction.transactionally)
        _ <- db.run(createSecondUserAction.transactionally)
        _ <- db.run(createPhotoAction.transactionally)
        _ <- db.run(createThirdPhotoAction.transactionally)
        _ <- db.run(createFourthPhotoAction.transactionally)
        photo <- repository.get(photoId, 1)
      } yield photo
    }

    it("should return a photo") {
      mockPresignUrl(Some("mock-photo-1.jpg"), None, 1)

      subject(1).map {
        case Some(_) => succeed
        case None    => fail("Photo should be found")
      }
    }

    describe("when there is a fileName") {
      it("should add the pre-signed url to the source attribute") {
        mockPresignUrl(Some("mock-photo-1.jpg"), Some(mockUrl), 1)

        subject(1).map {
          case Some(photo) => {
            assert(photo.source.contains(mockUrl.toString))
          }
          case None => fail("Photo should be found")
        }
      }
    }

    describe("when there is not a fileName") {
      it("should not add the pre-signed url to the source attribute") {
        subject(4).map {
          case Some(photo) => {
            assert(photo.id == 4)
          }
          case None => fail("Photo should be found")
        }
      }
    }

    it("should not return another users photo") {
      subject(2).map {
        case Some(_) => fail("Photo should not be found")
        case None    => succeed
      }
    }
  }

  describe("#list") {
    val mockUrl = new URL("https://example.com")

    def subject(photoId: Int): Future[List[Photo]] = {
      for {
        _ <- db.run(createUserAction.transactionally)
        _ <- db.run(createSecondUserAction.transactionally)
        _ <- db.run(createPhotoAction.transactionally)
        _ <- db.run(createSecondPhotoAction.transactionally)
        _ <- db.run(createThirdPhotoAction.transactionally)
        _ <- db.run(createFourthPhotoAction.transactionally)
        photos <- repository.list(mockUserId)
      } yield photos
    }

    describe("when photos are found") {
      it("should return a list of photos") {
        mockPresignUrl(Some("mock-photo-1.jpg"), None, 1)
        mockPresignUrl(Some("mock-photo-2.jpg"), None, 1)

        subject(1).map {
          case (photos: List[_]) => {
            assert(photos.length == 3)
            assert(photos.head.creatorId == mockUserId)
          }
          case _ => fail("Photos not found")
        }
      }

      describe("when the photos have a source") {
        it("should add the pre-signed url to the source attribute") {
          mockPresignUrl(Some("mock-photo-1.jpg"), Some(mockUrl), 1)
          mockPresignUrl(Some("mock-photo-2.jpg"), Some(mockUrl), 1)

          subject(1).map {
            case (photos: List[_]) => {
              assert(photos.head.source.contains(mockUrl.toString))
            }
            case _ => fail("Photos not found")
          }
        }
      }

      describe("when the photos do not have a source") {
        it("should not add the pre-signed url to the source attribute") {
          mockPresignUrl(None, None, 1)
          mockPresignUrl(None, None, 1)

          subject(1).map {
            case (photos: List[_]) => {
              assert(photos.head.source.isEmpty)
            }
            case _ => fail("Photos not found")
          }
        }
      }
    }

    describe("when no photos are found") {
      it("should return an empty list") {
        for {
          photo <- repository.list(mockUserId)
        } yield photo match {
          case List(_) => fail("Photos found")
          case List()  => succeed
        }
      }
    }
  }

  describe("#update") {
    def updatePhoto(photoId: Int, userId: Int): Future[Option[Photo]] = {
      val createPhotoActionToUpdate =
        sqlu"""INSERT INTO photos (id, title, creator_id) VALUES(${photoId}, 'My great photo', 1)"""

      for {
        _ <- db.run(createUserAction.transactionally)
        _ <- db.run(createSecondUserAction.transactionally)
        _ <- db.run(createPhotoActionToUpdate.transactionally)
        photo <- repository.update(
          photoId,
          userId,
          mockPhoto.copy(
            id = photoId,
            description = Some("New description"),
            creatorId = 1
          )
        )
      } yield photo
    }

    describe("on success") {
      val existingPhotoId = 1

      describe("when the photo was created by the user") {
        it("should update a photo") {
          for {
            photo <- updatePhoto(existingPhotoId, 1)
          } yield photo match {
            case Some(photo) =>
              assert(photo.description.contains("New description"))
              assert(photo.updatedAt.isDefined)
            case None => fail("Photo not found")
          }
        }
      }

      describe("when the photo was not created by the user") {
        it("should not allow updating of another users photo") {
          for {
            photo <- updatePhoto(existingPhotoId, 2)
          } yield photo match {
            case Some(_) => {
              println(photo)
              fail("Photo should not be updated")
            }
            case None => succeed
          }
        }
      }

      describe("when trying to update a photo that doesn't exist") {
        val nonExistingPhotoId = 2

        it("should return none") {
          for {
            photo <- updatePhoto(nonExistingPhotoId, 1)
          } yield photo match {
            case Some(photo) =>
              assert(photo.description.contains("New description"))
            case None => fail("Photo not found")
          }
        }
      }
    }
  }

  describe("#uploadObject") {
    val mockUploadResult = new PutObjectResult()

    def mockUploadObject(): Unit = {
      (mockStorageRepository.uploadObject _)
        .expects(*, *, *)
        .returning(Future(Success(mockUploadResult)))
    }

    def subject(photoId: Int): Future[Try[PutObjectResult]] = {
      for {
        _ <- db.run(createUserAction.transactionally)
        _ <- db.run(createPhotoAction.transactionally)
        result <- repository.uploadObject(
          photoId,
          1,
          "file.jpg",
          new File("test.jpg")
        )
      } yield result
    }

    describe("when the photo does not exists") {
      it("should raise an exception") {
        recoverToExceptionIf[IllegalStateException](subject(2)).map { result =>
          result.getMessage should include("Photo does not exist")
        }
      }
    }

    describe("when the photo exists") {
      val session = db.createSession()

      describe("when the upload to storage succeeds") {
        it("should upload the file to the storage") {
          mockPresignUrl(Some("mock-photo-1.jpg"), None, 1)
          mockUploadObject()

          subject(1).map { result =>
            assert(result.isSuccess)
          }
        }

        it("should update the db photo with a filePath") {
          mockPresignUrl(Some("mock-photo-1.jpg"), None, 1)
          mockUploadObject()

          subject(1).map { _ =>
            val result = session
              .createStatement()
              .executeQuery("SELECT * FROM photos WHERE id = 1");
            assert(result.next())
            assert(result.getString("file_name") == "1/jpg/1.jpg")
          }
        }
      }

      describe("when the upload to storage fails") {
        it("should raise an exception") {
          mockPresignUrl(Some("mock-photo-1.jpg"), None, 1)
          (mockStorageRepository.uploadObject _)
            .expects(*, *, *)
            .returning(Future.failed(new Exception("Failed to upload object")))

          recoverToExceptionIf[Exception](subject(1)).map { result =>
            result.getMessage should include("Failed to upload object")
          }
        }
      }
    }
  }

  describe("#delete") {
    val session = db.createSession()

    describe("when the user is the creator of the photo") {
      it("should delete the photo") {
        for {
          _ <- db.run(createUserAction.transactionally)
          _ <- db.run(createPhotoAction.transactionally)
          _ <- repository.delete(1, mockUserId)
        } yield {
          val result = session
            .createStatement()
            .executeQuery("SELECT * FROM photos WHERE id = 1");
          assert(!result.next())
        }
      }
    }

    describe("when the user is not the creator of the photo") {
      it("should not delete the photo") {
        for {
          _ <- db.run(createUserAction.transactionally)
          _ <- db.run(createPhotoAction.transactionally)
          _ <- repository.delete(1, mockUserId + 1)
        } yield {
          val result = session
            .createStatement()
            .executeQuery("SELECT * FROM photos WHERE id = 1");
          assert(result.next())
        }
      }
    }
  }

  describe("#deleteObject") {
    def mockDeleteObject(): Unit = {
      (mockStorageRepository.deleteObject _)
        .expects(*, *)
        .returning(Future.successful())
    }

    def subject(photoId: Int): Future[Unit] = {
      for {
        _ <- db.run(createUserAction.transactionally)
        _ <- db.run(createPhotoAction.transactionally)
        result <- repository.deleteObject(
          photoId,
          mockUserId
        )
      } yield result
    }

    describe("when the photo does not exists") {
      it("should raise an exception") {
        recoverToExceptionIf[IllegalStateException](subject(2)).map { result =>
          result.getMessage should include("Photo does not exist")
        }
      }
    }

    describe("when the photo exists") {
      val session = db.createSession()

      describe("when the delete operation succeeds") {
        it("should delete the photo object from storage") {
          mockPresignUrl(Some("mock-photo-1.jpg"), None, 1)
          mockDeleteObject()

          subject(1).map { result =>
            assert(result == ())
          }
        }

        describe("when the photo update succeeds") {
          it("should update the db photo with a fileName of null") {
            mockPresignUrl(Some("mock-photo-1.jpg"), None, 1)
            mockDeleteObject()

            subject(1).map { _ =>
              val result = session
                .createStatement()
                .executeQuery("SELECT * FROM photos WHERE id = 1");
              assert(result.next())
              assert(result.getString("file_name") == null)
            }
          }
        }
      }

      describe("when the delete operation fails") {
        it("should raise an exception") {
          mockPresignUrl(Some("mock-photo-1.jpg"), None, 1)
          (mockStorageRepository.deleteObject _)
            .expects(*, *)
            .returning(Future.failed(new Exception("Failed to delete object")))

          recoverToExceptionIf[Exception](subject(1)).map { result =>
            result.getMessage should include("Failed to delete object")
          }
        }
      }
    }
  }
}
