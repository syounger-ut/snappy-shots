import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{
  Bucket,
  CreateBucketRequest,
  PutObjectRequest
}

import java.nio.file.{Files, Paths}

object S3Access {
  private val BUCKET_NAME = "snappy-shots"
  val FILE_PATH = "./snappy-shots"
  val FILE_NAME = "snappy-shots"
  private val AWS_ACCESS_KEY =
    sys.env.getOrElse("SNAPPY_SHOTS_AWS_ACCESS_KEY", "aws-access-key")
  private val AWS_SECRET =
    sys.env.getOrElse("SNAPPY_SHOTS_AWS_SECRET", "aws-secret-key")

  // Create a credentials provider chain
  val credentials = new BasicAWSCredentials(
    AWS_ACCESS_KEY,
    AWS_SECRET
  )

  // Create an S3 client
  private val client = AmazonS3ClientBuilder
    .standard()
    .withCredentials(new AWSStaticCredentialsProvider(credentials))
    .withRegion(Regions.EU_WEST_2)
    .build();

  // Check if the bucket already exists; if not, create it.
  if (client.doesBucketExist(BUCKET_NAME)) {
    println(
      "Bucket name is not available." + " Try again with a different Bucket name."
    )
  } else {
    // Create a bucket with the specified name
    val bucketRequest = new CreateBucketRequest(BUCKET_NAME)
    client.createBucket(bucketRequest)
  }

  // List the buckets in your account
  private val buckets = client.listBuckets()
  buckets.toArray.foreach(bucket => {
    println(bucket.asInstanceOf[Bucket].getName)
  })

  // Upload a file as a new object with ContentType and title specified.
  val request = new PutObjectRequest(
    BUCKET_NAME,
    "snappy-shots.txt",
    Paths.get("../../../Downloads/test.txt").toFile
  )
  client.putObject(request)
}
