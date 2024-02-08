package com.authService.repositories

import com.amazonaws.services.s3.model.{Bucket, PutObjectResult}
import com.google.inject.Inject

import java.io.File
import java.net.URL
import java.util.Date
import scala.concurrent.Future
import scala.util.Try

class StorageRepository @Inject() (storageAdapter: StorageAdapter) {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val DURATION_30_MINUTES = 60 * 60 * 30

  /* Create a new bucket
   * @param bucketName: String
   * @return Bucket
   */
  def createBucket(bucketName: String): Try[Bucket] = {
    Try(storageAdapter.bucketExists(bucketName)).map {
      case false => storageAdapter.createBucket(bucketName)
      case true  => throw new IllegalStateException("Bucket already exists")
    }
  }

  /* Get a pre-signed URL for a file
   * @param bucketName: String
   * @param fileName: String
   * @return URL
   */
  def preSignedUrl(bucketName: String, fileName: String): Future[URL] = {
    Future.fromTry(Try(storageAdapter.objectExists(bucketName, fileName))).map {
      case true =>
        storageAdapter.preSignedUrl(bucketName, fileName, urlExpiration)
      case false => throw new IllegalStateException("File does not exist")
    }
  }

  /* Upload a file to a bucket
   * @param bucketName: String
   * @param file: File
   * @return PutObjectResult
   */
  def uploadObject(
    bucketName: String,
    fileName: String,
    file: File
  ): Future[Try[PutObjectResult]] = {
    Future.fromTry(Try(storageAdapter.objectExists(bucketName, fileName))).map {
      case false => Try(storageAdapter.uploadObject(bucketName, fileName, file))
      case true  => throw new IllegalStateException("File already exists")
    }
  }

  /* Delete a file from a bucket
   * @param bucketName: String
   * @param fileName: String
   * @return Unit
   */
  def deleteObject(bucketName: String, fileName: String): Future[Unit] = {
    Future.fromTry(Try(storageAdapter.objectExists(bucketName, fileName))).map {
      case true  => storageAdapter.deleteObject(bucketName, fileName)
      case false => throw new IllegalStateException("File does not exist")
    }
  }

  private def urlExpiration: Date = {
    val expiration = new Date
    var expTimeMillis = expiration.getTime
    expTimeMillis += DURATION_30_MINUTES
    expiration.setTime(expTimeMillis)

    expiration
  }
}
