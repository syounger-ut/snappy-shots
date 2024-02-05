package com.authService.repositories

import com.amazonaws.services.s3.model.{Bucket, PutObjectResult}
import com.google.inject.Inject

import java.io.File
import scala.concurrent.Future
import scala.util.Try

class StorageRepository @Inject() (storageAdapter: StorageAdapter) {
  import scala.concurrent.ExecutionContext.Implicits.global

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
}
