package com.authService.repositories

import com.authService.DbUnitSpec
import com.authService.models.Photo

import java.time.Instant
import scala.concurrent.Future

class PhotoRepositorySpec extends DbUnitSpec {
  val repository = new PhotoRepository

  import profile.api._

  val mockUserId = 1
  val mockUserIdTwo = 2
  val mockPhoto: Photo = Photo(
    id = 0,
    title = "My wonderful photo",
    creator_id = mockUserId,
    description = None,
    source = None,
    created_at = Some(Instant.now()),
    updated_at = Some(Instant.now())
  )

  val createUserAction =
    sqlu"""INSERT INTO users (id, email, password) VALUES(1, 'foo@bar.com', 'password')"""
  val createSecondUserAction =
    sqlu"""INSERT INTO users (id, email, password) VALUES(2, 'bar@foo.com', 'password')"""
  val createPhotoAction =
    sqlu"""INSERT INTO photos (id, title, creator_id) VALUES(1, 'My great photo', ${mockUserId})"""
  val createSecondPhotoAction =
    sqlu"""INSERT INTO photos (id, title, creator_id) VALUES(2, 'My great photo', ${mockUserIdTwo})"""

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
    it("should return a photo") {
      for {
        _ <- db.run(createUserAction.transactionally)
        _ <- db.run(createPhotoAction.transactionally)
        photo <- repository.get(1, 1)
      } yield photo match {
        case Some(_) => succeed
        case None    => fail("Photo should be found")
      }
    }

    it("should not return another users photo") {
      for {
        _ <- db.run(createUserAction.transactionally)
        _ <- db.run(createSecondUserAction.transactionally)
        _ <- db.run(createPhotoAction.transactionally)
        _ <- db.run(createSecondPhotoAction.transactionally)
        photo <- repository.get(2, 1)
      } yield photo match {
        case Some(_) => fail("Photo should not be found")
        case None    => succeed
      }
    }
  }

  describe("#list") {
    describe("when photos are found") {
      it("should return a list of photos") {
        for {
          _ <- db.run(createUserAction.transactionally)
          _ <- db.run(createSecondUserAction.transactionally)
          _ <- db.run(createPhotoAction.transactionally)
          _ <- db.run(createSecondPhotoAction.transactionally)
          photo <- repository.list(mockUserId)
        } yield photo match {
          case List(_) => {
            assert(photo.length == 1)
            assert(photo.head.creator_id == mockUserId)
          }
          case List() => fail("Photos not found")
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
            creator_id = 1
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

  describe("#delete") {
    val session = db.createSession()

    it("should delete the photo") {
      for {
        _ <- db.run(createUserAction.transactionally)
        _ <- db.run(createPhotoAction.transactionally)
        _ <- repository.delete(1)
      } yield {
        val result = session
          .createStatement()
          .executeQuery("SELECT * FROM photos WHERE id = 1");
        assert(!result.next())
      }
    }
  }
}
