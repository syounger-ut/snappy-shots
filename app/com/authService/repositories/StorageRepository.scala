package com.authService.repositories

import com.amazonaws.services.s3.model.Bucket

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
}
