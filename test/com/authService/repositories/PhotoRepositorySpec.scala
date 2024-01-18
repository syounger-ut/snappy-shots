package com.authService.repositories

import com.authService.DbUnitSpec
import com.authService.models.Photo

import java.time.Instant
import scala.concurrent.Future

class PhotoRepositorySpec extends DbUnitSpec {
  val repository = new PhotoRepository

  import profile.api._

  val mockPhoto: Photo = Photo(
    id = 0,
    title = "My wonderful photo",
    creator_id = 1,
    description = None,
    source = None,
    created_at = Some(Instant.now()),
    updated_at = Some(Instant.now())
  )

  describe("#create") {
    describe("on success") {
      it("should create a photo") {
        val createUserAction =
          sqlu"""INSERT INTO users (id, email, password) VALUES(1, 'foo@bar.com', 'password')"""
        val createUserQuery = db.run(createUserAction)

        for {
          _ <- createUserQuery
          photo <- repository.create(mockPhoto)
        } yield photo match {
          case photo =>
            assert(photo.id == 1)
            assert(photo.title == mockPhoto.title)
        }
      }
    }

    describe("on failure") {
      it("should throw an exception") {
        val createUserAction = sqlu"""DELETE FROM users WHERE id = 1"""
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

  describe("#update") {
    def updatePhoto(photoId: Int): Future[Option[Photo]] = {
      val createUserAction =
        sqlu"""INSERT INTO users (id, email, password) VALUES(1, 'foo@bar.com', 'password')"""
      val createPhotoAction =
        sqlu"""INSERT INTO photos (id, title, creator_id) VALUES(${photoId}, 'My great photo', 1)"""

      for {
        _ <- db.run(createUserAction.transactionally)
        _ <- db.run(createPhotoAction.transactionally)
        photo <- repository.update(mockPhoto.copy(id = 1, description = Some("New description"), creator_id = 1))
      } yield photo
    }

    describe("on success") {
      val existingPhotoId = 1

      it("should update a photo") {
        for {
          photo <- updatePhoto(existingPhotoId)
        } yield photo match {
          case Some(photo) =>
            assert(photo.description.contains("New description"))
          case None => fail("Photo not found")
        }
      }
    }

    describe("on failure") {
      val nonExistingPhotoId = 2

      it("should return none") {
        for {
          photo <- updatePhoto(nonExistingPhotoId)
        } yield photo match {
          case Some(photo) =>
            assert(photo.description.contains("New description"))
          case None => fail("Photo not found")
        }
      }
    }
  }
}
