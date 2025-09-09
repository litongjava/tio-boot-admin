package com.litongjava.tio.boot.admin.utils;

import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.ContentTypeUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class AwsS3Utils {

  public static final String urlFormat = "https://%s.s3.%s.amazonaws.com/%s";
  public static final String bucketName = EnvUtils.get("AWS_S3_BUCKET_NAME");
  public static final String regionName = EnvUtils.get("AWS_S3_REGION_NAME");
  public static final String accessKeyId = EnvUtils.get("AWS_S3_ACCESS_KEY_ID");
  public static final String secretAccessKey = EnvUtils.get("AWS_S3_SECRET_ACCESS_KEY");

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

  public static String getUrl(String targetName) {
    return String.format(AwsS3Utils.urlFormat, AwsS3Utils.bucketName, regionName, targetName);
  }

  public static String getUrl(String bucketName, String targetName) {
    return String.format(AwsS3Utils.urlFormat, bucketName, regionName, targetName);
  }

  public static S3Client buildClient() {
    AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider.create(awsCreds);

    Region region = Region.of(regionName);
    // 创建S3客户端
    S3ClientBuilder builder = S3Client.builder();

    S3Client s3 = builder.region(region) //
        .credentialsProvider(staticCredentialsProvider) //
        .build();

    return s3;

  }
}
