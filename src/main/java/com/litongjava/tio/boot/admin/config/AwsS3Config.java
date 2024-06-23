package com.litongjava.tio.boot.admin.config;

import com.litongjava.tio.utils.environment.EnvUtils;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

public class AwsS3Config {

  public S3Client buildClient() {
    String accessKeyId = EnvUtils.get("AWS_S3_ACCESS_KEY_ID");
    String secretAccessKey = EnvUtils.get("AWS_S3_SECRET_ACCESS_KEY");
    String regionName = EnvUtils.get("AWS_S3_REGION_NAME");

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
