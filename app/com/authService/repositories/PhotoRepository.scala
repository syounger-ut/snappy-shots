package com.authService.repositories

import com.authService.models._
import com.authService.utils.{Connection, Profile, SlickDBDriver}
import com.google.inject.Inject
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PhotoRepository @Inject() (
  override val profile: JdbcProfile = SlickDBDriver.getDriver
) extends PhotosTable
  with Profile {
  import scala.concurrent.ExecutionContext.Implicits.global

  import profile.api._

  val db = new Connection(profile).db()

  def create(photo: Photo): Future[Photo] = {
    val insertQuery =
      photos returning photos.map(_.id) into ((photo, id) =>
        photo.copy(id = id)
      )
    val action = insertQuery += photo

    db.run(action.asTry).map {
      case Success(photo: Photo) => photo
      case Failure(exception: Exception) =>
        throw new IllegalStateException(exception.getMessage)
    }
  }
}
