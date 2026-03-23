package com.litongjava.tio.boot.admin.utils.storage;

import com.aliyun.oss.OSS;
import com.litongjava.tio.boot.admin.consts.StoragePlatformConst;
import com.litongjava.tio.boot.admin.utils.CloudflareR2Utils;
import com.litongjava.tio.boot.admin.utils.TioAdminEnvUtils;
import com.qcloud.cos.COSClient;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
public class UniStorageUtils {

  public static final String storagePlatform = TioAdminEnvUtils.getStoragePlatform();

  public static String upload(String targetName, byte[] fileContent, String suffix) {
    String etag = null;

    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      try (S3Client client = AwsS3Utils.buildClient();) {
        PutObjectResponse response = AwsS3Utils.upload(client, targetName, fileContent, suffix);
        etag = response.eTag();
      } catch (Exception e) {
        log.error("Error uploading file", e);
        throw new RuntimeException(e);
      }
    } else if (StoragePlatformConst.tencent_cos.equals(storagePlatform)) {
      COSClient cosClient = null;
      try {
        cosClient = TencentCOSUtils.buildClient();
        etag = TencentCOSUtils.upload(cosClient, targetName, fileContent, suffix).getETag();
      } catch (Exception e) {
        log.error("Error uploading file", e);
        throw new RuntimeException(e);
      } finally {
        if (cosClient != null) {
          cosClient.shutdown();
        }
      }

    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {
      OSS client = null;
      try {
        client = AliyunOssUtils.buildClient();
        return AliyunOssUtils.upload(client, targetName, fileContent, suffix).getETag();
      } catch (Exception e) {
        log.error("Error uploading file", e);
        throw new RuntimeException(e);
      } finally {
        if (client != null) {
          client.shutdown();
        }
      }
    } else {
      try (S3Client client = CloudflareR2Utils.buildClient();) {
        PutObjectResponse response = CloudflareR2Utils.upload(client, CloudflareR2Utils.bucketName, targetName,
            fileContent, suffix);
        etag = response.eTag();
      } catch (Exception e) {
        log.error("Error uploading file", e);
        throw new RuntimeException(e);
      }
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
