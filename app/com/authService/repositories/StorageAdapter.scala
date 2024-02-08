package com.authService.repositories

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model.{
  Bucket,
  CreateBucketRequest,
  ObjectMetadata,
  PutObjectRequest,
  PutObjectResult
}

import java.io.File
import java.net.URL
import java.util.Date

trait IStorageAdapter {
  def bucketExists(bucketName: String): Boolean

  def createBucket(bucketName: String): Bucket

  def objectExists(bucketName: String, fileName: String): Boolean

  def preSignedUrl(bucketName: String, fileName: String, date: Date): URL

  def uploadObject(
    bucketName: String,
    fileName: String,
    file: File
  ): PutObjectResult

  def deleteObject(
    bucketName: String,
    fileName: String
  ): Unit
}

trait IClientBuilder {
  def build: AmazonS3
}

// $COVERAGE-OFF$
class StorageAdapter extends IStorageAdapter {
  private val AWS_ACCESS_KEY =
    sys.env.getOrElse("SNAPPY_SHOTS_AWS_ACCESS_KEY", "aws-access-key")
  private val AWS_SECRET =
    sys.env.getOrElse("SNAPPY_SHOTS_AWS_SECRET", "aws-secret-key")

  def bucketExists(bucketName: String): Boolean =
    client.doesBucketExistV2(bucketName)

  def createBucket(bucketName: String): Bucket = {
    val bucketRequest = new CreateBucketRequest(bucketName)
    client.createBucket(bucketRequest)
  }

  def objectExists(bucketName: String, fileName: String): Boolean =
    client.doesObjectExist(bucketName, fileName)

  def preSignedUrl(
    bucketName: String,
    fileName: String,
    expiration: Date
  ): URL = client.generatePresignedUrl(bucketName, fileName, expiration)

  def uploadObject(
    bucketName: String,
    fileName: String,
    file: File
  ): PutObjectResult = {
    val request = new PutObjectRequest(bucketName, fileName, file)
    val metadata = new ObjectMetadata()
    metadata.setContentType("plain/text")
    metadata.addUserMetadata("title", "someTitle")
    request.setMetadata(metadata)

    client.putObject(request)
  }

  def deleteObject(bucketName: String, fileName: String): Unit =
    client.deleteObject(bucketName, fileName)

  private def client = new ClientBuilder(AmazonS3ClientBuilder.standard()).build

  private class ClientBuilder(awsClientBuilder: AmazonS3ClientBuilder)
    extends IClientBuilder {
    def build: AmazonS3 = {
      val credentials = new BasicAWSCredentials(
        AWS_ACCESS_KEY,
        AWS_SECRET
      )

      awsClientBuilder
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withRegion(Regions.EU_WEST_2)
        .build();
    }
  }
}
// $COVERAGE-ON$
