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

  def list(userId: Long): Future[List[Photo]] = {
    val query = photos
      .filter(_.creator_id === userId)
      .result

    db.run(query).map(_.toList)
  }

  def get(id: Long, userId: Long): Future[Option[Photo]] = {
    val query = photos
      .filter(table =>
        table.id === id &&
          table.creator_id === userId
      )
      .result
      .headOption
    db.run(query)
  }

  def update(
    photoId: Long,
    userId: Long,
    photo: Photo
  ): Future[Option[Photo]] = {
    val action = photos
      .filter(table => table.id === photoId && table.creator_id === userId)
      .map(photo =>
        (photo.title, photo.description, photo.source, photo.creator_id)
      )
      .update((photo.title, photo.description, photo.source, photo.creator_id))

    db.run(action.asTry).map {
      case Success(1) => Some(photo)
      case Success(0) => None
      case Failure(e: Exception) =>
        throw new IllegalStateException(e.getMessage)
    }
  }

  def delete(id: Long, userId: Long): Future[Int] = {
    val action = photos
      .filter(table => table.id === id && table.creator_id === userId)
      .delete
    db.run(action)
  }
}
