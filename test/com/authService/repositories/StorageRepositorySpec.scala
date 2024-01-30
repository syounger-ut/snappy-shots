package com.authService.repositories

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.Bucket
import com.authService.UnitSpec

class StorageRepositorySpec extends UnitSpec {
  val mockStorageAdapter: StorageAdapter = mock[StorageAdapter]
  val repository = new StorageRepository(mockStorageAdapter)
  val bucketName = "mock-bucket-name"

  describe("#createBucket") {
    def mockBucketExists(returns: Boolean) = {
      (mockStorageAdapter.bucketExists _)
        .expects(bucketName)
        .returning(returns)
    }

    def mockCreateBucket() = {
      (mockStorageAdapter.createBucket _)
        .expects(bucketName)
        .returning(new Bucket())
    }

    describe("when the bucket does not exist") {
      describe("when the create bucket operation is a success") {
        it("should create a bucket") {
          mockBucketExists(false)
          mockCreateBucket()

          val bucket = repository.createBucket(bucketName)
          assert(bucket.isSuccess)
        }
      }

      describe("when the create bucket operation is not a success") {
        it("should fail to create a bucket") {
          mockBucketExists(false)
          (mockStorageAdapter.createBucket _)
            .expects(bucketName)
            .throwing(new RuntimeException())

          val bucket = repository.createBucket(bucketName)
          assert(bucket.isFailure)
        }
      }
    }

    describe("when the bucket already exists") {
      it("should fail to create a bucket") {
        mockBucketExists(true)

        val bucket = repository.createBucket(bucketName)
        assert(bucket.isFailure)
      }
    }

    describe("when the bucketExists operation fails") {
      it("should fail to create a bucket") {
        (mockStorageAdapter.bucketExists _)
          .expects(bucketName)
          .throwing(new RuntimeException())

        val bucket = repository.createBucket(bucketName)
        assert(bucket.isFailure)
      }
    }
  }
}
