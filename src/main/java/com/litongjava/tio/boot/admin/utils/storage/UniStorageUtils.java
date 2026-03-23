package com.litongjava.tio.boot.admin.utils.storage;

import com.litongjava.tio.boot.admin.consts.StoragePlatformConst;
import com.litongjava.tio.boot.admin.utils.TioAdminEnvUtils;
import com.qcloud.cos.COSClient;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class UniStorageUtils {

  public static final String storagePlatform = TioAdminEnvUtils.getStoragePlatform();

  public static String upload(String targetName, byte[] fileContent, String suffix) {
    String etag = null;

    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      try (S3Client client = AwsS3Utils.buildClient();) {
        PutObjectResponse response = AwsS3Utils.upload(client, targetName, fileContent, suffix);
        etag = response.eTag();
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    } else if (StoragePlatformConst.tencent_cos.equals(storagePlatform)) {
   
      
    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {

    }

    else {

    }

    return etag;
  }

  public static String getUrl(String bucketName, String targetName) {
    // TODO Auto-generated method stub
    return null;
  }

  public static String getUrl(String targetName) {
    // TODO Auto-generated method stub
    return null;
  }

  public static String getPresignedDownloadUrl(String targetName) {
    // TODO Auto-generated method stub
    return null;
  }

  public static String getPresignedDownloadUrl(String bucket, String targetName) {
    // TODO Auto-generated method stub
    return null;
  }

  public static String getPresignedDownloadUrl(String region, String bucket, String targetName) {
    // TODO Auto-generated method stub
    return null;
  }

  public static String getBucketName() {
    // TODO Auto-generated method stub
    return null;
  }

  public static String getRegionName() {
    // TODO Auto-generated method stub
    return null;
  }

}
