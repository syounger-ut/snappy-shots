package com.authService.repositories

import com.amazonaws.services.s3.model.{Bucket, PutObjectResult}

import java.io.File
import scala.util.Try

class StorageRepository(storageAdapter: StorageAdapter) {
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
  def uploadObject(bucketName: String, file: File): Try[PutObjectResult] = {
    if (storageAdapter.objectExists(bucketName, file.getName)) {
      throw new IllegalStateException("File already exists in bucket")
    } else {
      Try(storageAdapter.uploadObject(bucketName, file.getName, file))
    }
  }
}
