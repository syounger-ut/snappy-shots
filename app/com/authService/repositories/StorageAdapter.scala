package com.authService.repositories

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model.{Bucket, CreateBucketRequest}

trait IStorageAdapter {
  def createBucket(bucketName: String): Bucket

  def bucketExists(bucketName: String): Boolean
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

  def bucketExists(bucketName: String): Boolean =
    client.doesBucketExistV2(bucketName)

  def createBucket(bucketName: String): Bucket = {
    val bucketRequest = new CreateBucketRequest(bucketName)
    client.createBucket(bucketRequest)
  }

  private def client = new ClientBuilder(AmazonS3ClientBuilder.standard()).build
}
// $COVERAGE-ON$
