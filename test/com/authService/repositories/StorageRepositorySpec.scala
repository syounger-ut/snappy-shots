package com.authService.repositories

import com.amazonaws.services.s3.model.{Bucket, PutObjectResult}
import com.authService.{AsyncUnitSpec, UnitSpec}

import java.io.File
import scala.concurrent.Future

class StorageRepositorySpec extends AsyncUnitSpec {
  val mockStorageAdapter: StorageAdapter = mock[StorageAdapter]
  val repository = new StorageRepository(mockStorageAdapter)
  val bucketName = "mock-bucket-name"

  def mockObjectExists(returns: Boolean): Unit = {
    (mockStorageAdapter.objectExists _)
      .expects(bucketName, *)
      .returning(returns)
  }

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

  describe("#upload") {
    def mockUploadObject(file: File, result: PutObjectResult): Unit = {
      (mockStorageAdapter.uploadObject _)
        .expects(bucketName, file.getName, file)
        .returning(result)
    }

    describe("when the file already exists in the bucket") {
      it("should fail to upload the file") {
        mockObjectExists(true)

        val mockFile = new File("test/resources/test.txt")

        recoverToSucceededIf[IllegalStateException] {
          repository
            .uploadObject(bucketName, mockFile.getName, mockFile)
        }
      }
    }

    describe("when the file does not exist in the bucket") {
      describe("when the upload operation succeeds") {
        it("should upload a file") {
          mockObjectExists(false)
          val mockFile = new File("test/resources/test.txt")
          val mockResult = new PutObjectResult()
          mockUploadObject(mockFile, mockResult)

          val result =
            repository.uploadObject(bucketName, mockFile.getName, mockFile)

          result.map { result =>
            assert(result.isSuccess)
            assert(result.get == mockResult)
          }
        }
      }

      describe("when the upload operation fails") {
        it("should fail to upload a file") {
          mockObjectExists(false)
          val mockFile = new File("test/resources/test.txt")
          val mockException = new RuntimeException("Upload failed")
          (mockStorageAdapter.uploadObject _)
            .expects(bucketName, mockFile.getName, mockFile)
            .throwing(mockException)

          val result =
            repository.uploadObject(bucketName, mockFile.getName, mockFile)

          result.map { result =>
            assert(result.isFailure)
          }
        }
      }
    }
  }

  describe("#deleteObject") {
    def mockDeleteObject(fileName: String): Unit = {
      (mockStorageAdapter.deleteObject _)
        .expects(bucketName, fileName)
        .returning(())
    }

    describe("when the file does not exist in the bucket") {
      it("should fail to delete the file") {
        mockObjectExists(false)

        recoverToSucceededIf[IllegalStateException] {
          repository.deleteObject(bucketName, "test.txt")
        }.recover { case e: Throwable =>
          assert(e.getMessage == "File does not exist")
        }
      }
    }

    describe("when the file exists in the bucket") {
      describe("when the delete operation succeeds") {
        it("should delete the file") {
          mockObjectExists(true)
          mockDeleteObject("test.txt")

          val result = repository.deleteObject(bucketName, "test.txt")
          result.map { result =>
            assert(result == ())
          }
        }
      }

      describe("when the delete operation fails") {
        it("should fail to delete the file") {
          mockObjectExists(true)
          (mockStorageAdapter.deleteObject _)
            .expects(bucketName, "test.txt")
            .returning(Future.failed(new RuntimeException("Delete failed")))

          val result = repository.deleteObject(bucketName, "test.txt")
          result.map { result =>
            assert(result == ())
          }
        }
      }
    }
  }
}
