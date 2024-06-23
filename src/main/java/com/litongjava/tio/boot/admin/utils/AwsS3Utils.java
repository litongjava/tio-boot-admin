package com.litongjava.tio.boot.admin.utils;

import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.ContentTypeUtils;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class AwsS3Utils {

  public static final String urlFormat = "https://%s.s3.us-west-1.amazonaws.com/%s";
  public static final String bucketName = EnvUtils.get("AWS_S3_BUCKET_NAME");
  public static final String regionName = EnvUtils.get("AWS_S3_REGION_NAME");

  public static PutObjectResponse upload(S3Client client, String bucketName, String targetName, byte[] fileContent,
      String suffix) {
    try {
      String contentType = ContentTypeUtils.getContentType(suffix);
      PutObjectRequest putOb = PutObjectRequest.builder().bucket(bucketName).key(targetName).contentType(contentType)
          .build();

      PutObjectResponse putObject = client.putObject(putOb, RequestBody.fromBytes(fileContent));
      return putObject;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
