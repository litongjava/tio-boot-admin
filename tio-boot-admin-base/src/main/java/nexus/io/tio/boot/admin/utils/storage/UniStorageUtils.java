package nexus.io.tio.boot.admin.utils.storage;

import lombok.extern.slf4j.Slf4j;
import nexus.io.tio.boot.admin.consts.StoragePlatformConst;
import nexus.io.tio.boot.admin.utils.TioAdminEnvUtils;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
public class UniStorageUtils {

  public static final String storagePlatform = TioAdminEnvUtils.getStoragePlatform();

  public static String upload(String targetName, byte[] fileContent, String suffix) {
    String etag = null;

    if (StoragePlatformConst.aws_s3.equals(storagePlatform)) {
      PutObjectResponse response = AwsS3Utils.upload(targetName, fileContent, suffix);
      etag = response.eTag();
    } else if (StoragePlatformConst.tencent_cos.equals(storagePlatform)) {
      etag = TencentCOSUtils.upload(targetName, fileContent, suffix).getETag();
    } else if (StoragePlatformConst.aliyun_oss.equals(storagePlatform)) {
      etag = AliyunOssUtils.upload(targetName, fileContent, suffix).getETag();
    } else {
      PutObjectResponse response = CloudflareR2Utils.upload(CloudflareR2Utils.bucketName, targetName, fileContent,
          suffix);
      etag = response.eTag();
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
