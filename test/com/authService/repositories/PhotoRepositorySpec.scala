package com.authService.repositories

import com.authService.DbUnitSpec
import com.authService.models.Photo

import java.time.Instant

class PhotoRepositorySpec extends DbUnitSpec {
  val repository = new PhotoRepository

  import profile.api._

  describe("#create") {
    val mockPhoto = Photo(
      id = 0,
      title = "My wonderful photo",
      creator_id = 1,
      description = None,
      source = None,
      created_at = Some(Instant.now()),
      updated_at = Some(Instant.now())
    )

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
}
