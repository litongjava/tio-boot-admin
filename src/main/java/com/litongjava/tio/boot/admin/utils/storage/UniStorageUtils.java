package com.litongjava.tio.boot.admin.utils.storage;

import com.aliyun.oss.OSS;
import com.litongjava.tio.boot.admin.consts.StoragePlatformConst;
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
    String result = null;

    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      result = AwsS3Utils.getUrl(bucketName, targetName);

    } else if (StoragePlatformConst.tencent_cos.equals(storagePlatform)) {
      result = TencentCOSUtils.getUrl(bucketName, targetName);

    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {
      result = AliyunOssUtils.getUrl(bucketName, targetName);

    } else {
      result = CloudflareR2Utils.getUrl(bucketName, targetName);
    }

    return result;
  }

  public static String getUrl(String targetName) {
    String result = null;

    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      result = AwsS3Utils.getUrl(targetName);

    } else if (StoragePlatformConst.tencent_cos.equals(storagePlatform)) {
      result = TencentCOSUtils.getUrl(targetName);

    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {
      result = AliyunOssUtils.getUrl(targetName);

    } else {
      result = CloudflareR2Utils.getUrl(targetName);
    }

    return result;
  }

  public static String getPresignedDownloadUrl(String targetName) {
    String result = null;

    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      result = AwsS3Utils.getPresignedDownloadUrl(targetName);

    } else if (StoragePlatformConst.tencent_cos.equals(storagePlatform)) {
      result = TencentCOSUtils.getPresignedDownloadUrl(targetName);

    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {
      result = AliyunOssUtils.getPresignedDownloadUrl(targetName);

    } else {
      result = CloudflareR2Utils.getPresignedDownloadUrl(targetName);
    }

    return result;
  }

  public static String getPresignedDownloadUrl(String bucket, String targetName) {
    String result = null;

    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      result = AwsS3Utils.getPresignedDownloadUrl(bucket, targetName);

    } else if (StoragePlatformConst.tencent_cos.equals(storagePlatform)) {
      result = TencentCOSUtils.getPresignedDownloadUrl(bucket, targetName);

    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {
      result = AliyunOssUtils.getPresignedDownloadUrl(bucket, targetName);

    } else {
      result = CloudflareR2Utils.getPresignedDownloadUrl(bucket, targetName);
    }

    return result;
  }

  public static String getPresignedDownloadUrl(String region, String bucket, String targetName) {
    String result = null;

    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      result = AwsS3Utils.getPresignedDownloadUrl(region, bucket, targetName);

    } else if (StoragePlatformConst.tencent_cos.equals(storagePlatform)) {
      result = TencentCOSUtils.getPresignedDownloadUrl(region, bucket, targetName);

    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {
      result = AliyunOssUtils.getPresignedDownloadUrl(region, bucket, targetName);

    } else {
      result = CloudflareR2Utils.getPresignedDownloadUrl(region, bucket, targetName);
    }

    return result;
  }

  public static String getBucketName() {
    String result = null;

    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      result = AwsS3Utils.getBucketName();

    } else if (StoragePlatformConst.tencent_cos.equals(storagePlatform)) {
      result = TencentCOSUtils.getBucketName();

    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {
      result = AliyunOssUtils.getBucketName();

    } else {
      result = CloudflareR2Utils.getBucketName();
    }

    return result;
  }

  public static String getRegionName() {
    String result = null;

    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      result = AwsS3Utils.getRegionName();

    } else if (StoragePlatformConst.tencent_cos.equals(storagePlatform)) {
      result = TencentCOSUtils.getRegionName();

    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {
      result = AliyunOssUtils.getRegionName();

    } else {
      result = CloudflareR2Utils.getRegionName();
    }

    return result;
  }

}
