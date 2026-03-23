package com.litongjava.tio.boot.admin.utils.storage;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class UniStorageUtils {

  public static String upload(String targetName, byte[] fileContent, String suffix) {
    try (S3Client client = AwsS3Utils.buildClient();) {
      PutObjectResponse response = AwsS3Utils.upload(client, targetName, fileContent, suffix);
      etag = response.eTag();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return null;
  }

  
}
