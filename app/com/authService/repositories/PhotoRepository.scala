package com.authService.repositories

import com.amazonaws.services.s3.model.PutObjectResult
import com.authService.models._
import com.authService.utils.{Connection, Profile, SlickDBDriver}
import com.google.inject.Inject
import slick.jdbc.JdbcProfile

import java.io.File
import java.time.Instant
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class PhotoRepository @Inject() (
  val storageRepository: StorageRepository = new StorageRepository(
    new StorageAdapter
  ),
  override val profile: JdbcProfile = SlickDBDriver.getDriver
) extends PhotosTable
  with Profile {
  import scala.concurrent.ExecutionContext.Implicits.global

  import profile.api._

  val db = new Connection(profile).db()

  def create(photo: Photo): Future[Photo] = {
    val createdAt = Some(Instant.now())
    val createPhoto = photo.copy(createdAt = createdAt, updatedAt = createdAt)
    val insertQuery =
      photos returning photos.map(_.id) into ((photo, id) =>
        photo.copy(id = id)
      )
    val action = insertQuery += createPhoto

    db.run(action.asTry).map {
      case Success(photo: Photo) => photo
      case Failure(exception: Exception) =>
        throw new IllegalStateException(exception.getMessage)
    }
  }

  def list(userId: Long): Future[List[Photo]] = {
    val query = photos
      .filter(_.creatorId === userId)
      .result

    for {
      photos <- db.run(query).map(_.toList)
      photosWithSources <- Future.sequence(
        photos.map(photo =>
          storageRepository
            .preSignedUrl("snappy-shots", photo.title)
            .map(pre_signed_url =>
              photo.copy(source = Some(pre_signed_url.toString))
            )
            .recover(_ => photo)
        )
      )
    } yield photosWithSources
  }

  def get(id: Long, userId: Long): Future[Option[Photo]] = {
    val query = photos
      .filter(table =>
        table.id === id &&
          table.creatorId === userId
      )
      .result
      .headOption

    for {
      photo <- db.run(query)
      pre_signed_url <- photo match {
        case Some(photo) =>
          storageRepository
            .preSignedUrl("snappy-shots", photo.title)
            .recover(_ => None)
        case None => Future(None)
      }
    } yield (photo, pre_signed_url) match {
      case (Some(photo), pre_signed_url) =>
        Some(photo.copy(source = Some(pre_signed_url.toString)))
      case (None, _) => None
    }
  }

  def update(
    photoId: Long,
    userId: Long,
    photo: Photo
  ): Future[Option[Photo]] = {
    val photoToUpdate = photo.copy(updatedAt = Some(Instant.now()))

    val action = photos
      .filter(table => table.id === photoId && table.creatorId === userId)
      .map(photo =>
        (
          photo.title,
          photo.description,
          photo.source,
          photo.creatorId,
          photo.updatedAt
        )
      )
      .update(
        (
          photoToUpdate.title,
          photoToUpdate.description,
          photoToUpdate.source,
          photoToUpdate.creatorId,
          photoToUpdate.updatedAt
        )
      )

    db.run(action.asTry).map {
      case Success(1) => Some(photo)
      case Success(0) => None
      case Failure(e: Exception) =>
        throw new IllegalStateException(e.getMessage)
    }
  }

  /* Upload a file to a bucket
   * @param photoId: Int
   * @param file: File
   * @param userId: Long
   * @return Future[Try[PutObjectResult]]
   */
  def uploadObject(
    photoId: Int,
    userId: Long,
    fileName: String,
    file: File
  ): Future[Try[PutObjectResult]] = {
    get(photoId, userId)
      .flatMap {
        case Some(_) => uploadToStorage(photoId, userId, fileName, file)
        case None    => throw new IllegalStateException("Photo does not exist")
      }
  }

  def delete(id: Long, userId: Long): Future[Int] = {
    val action = photos
      .filter(table => table.id === id && table.creatorId === userId)
      .delete
    db.run(action)
  }

  private def uploadToStorage(
    photoId: Int,
    userId: Long,
    fileName: String,
    file: File
  ): Future[Try[PutObjectResult]] = {
    val fileExtension = fileName.split('.').last
    val fileNameUpdate = s"$userId/$fileExtension/$photoId.$fileExtension"
    val bucketName = "snappy-shots"

    storageRepository.uploadObject(bucketName, fileNameUpdate, file)
  }
}
