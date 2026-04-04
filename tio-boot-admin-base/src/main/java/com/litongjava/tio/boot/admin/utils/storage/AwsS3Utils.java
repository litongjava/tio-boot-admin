package com.litongjava.tio.boot.admin.utils.storage;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import nexus.io.tio.utils.environment.EnvUtils;
import nexus.io.tio.utils.http.ContentTypeUtils;
import nexus.io.tio.utils.hutool.FilenameUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

public class AwsS3Utils {

  public static final String urlFormat = "https://%s.s3.%s.amazonaws.com/%s";

  public static final String bucketName = EnvUtils.get("AWS_S3_BUCKET_NAME");
  public static final String regionName = EnvUtils.get("AWS_S3_REGION_NAME");
  public static final String accessKeyId = EnvUtils.get("AWS_S3_ACCESS_KEY_ID");
  public static final String secretAccessKey = EnvUtils.get("AWS_S3_SECRET_ACCESS_KEY");
  public static final String domain = EnvUtils.getStr("AWS_S3_BUCKET_DOMAIN");
  public static final String AWS_PROFILE = EnvUtils.getStr("AWS_PROFILE");

  public static final Duration DEFAULT_PRESIGN_EXPIRES = Duration.ofMinutes(30);

  // -------------------------
  // Upload
  // -------------------------

  public static PutObjectResponse upload(S3Client client, String targetName, byte[] fileContent, String suffix) {
    // TODO Auto-generated method stub
    return upload(client, bucketName, targetName, fileContent, suffix);
  }

  public static PutObjectResponse upload(S3Client client, String bucketName, String targetName, byte[] fileContent,
      String suffix) {
    try {
      String contentType = ContentTypeUtils.getContentType(suffix);
      PutObjectRequest putOb = PutObjectRequest.builder().bucket(bucketName).key(targetName).contentType(contentType)
          .build();

      return client.putObject(putOb, RequestBody.fromBytes(fileContent));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static PutObjectResponse upload(S3Client client, String targetName, File file) {
    String name = file.getName();
    String suffix = FilenameUtils.getSuffix(name);
    String contentType = ContentTypeUtils.getContentType(suffix);
    try {
      PutObjectRequest putOb = PutObjectRequest.builder().bucket(bucketName).key(targetName).contentType(contentType)
          .build();

      return client.putObject(putOb, RequestBody.fromFile(file));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static PutObjectResponse upload(S3Client client, String bucketName, String targetName, File file) {
    String name = file.getName();
    String suffix = FilenameUtils.getSuffix(name);
    String contentType = ContentTypeUtils.getContentType(suffix);
    try {
      PutObjectRequest putOb = PutObjectRequest.builder().bucket(bucketName).key(targetName).contentType(contentType)
          .build();

      return client.putObject(putOb, RequestBody.fromFile(file));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // -------------------------
  // Public URL (only works if bucket/object is public or via domain/CDN)
  // -------------------------
  public static String getUrl(String targetUri) {
    if (domain != null) {
      return "https://" + domain + "/" + targetUri;
    } else {
      return String.format(AwsS3Utils.urlFormat, bucketName, regionName, targetUri);
    }
  }

  public static String getUrl(String bucketName, String targetUri) {
    if (domain != null) {
      return "https://" + domain + "/" + targetUri;
    } else {
      return String.format(AwsS3Utils.urlFormat, bucketName, regionName, targetUri);
    }
  }

  public static String getUrl(String regionName, String bucketName, String targetUri) {
    if (domain != null) {
      return "https://" + domain + "/" + targetUri;
    } else {
      return String.format(AwsS3Utils.urlFormat, bucketName, regionName, targetUri);
    }
  }

  // -------------------------
  // Presigned Download URL (works for private bucket)
  // -------------------------
  /**
   * 生成可下载的预签名 GET URL（默认 30 分钟）。
   * 适用于 bucket 私有的场景。
   */
  public static String getPresignedDownloadUrl(String targetUri) {
    return getPresignedDownloadUrl(regionName, bucketName, targetUri, DEFAULT_PRESIGN_EXPIRES, null, null);
  }

  public static String getPresignedDownloadUrl(String bucket, String targetUri) {
    return getPresignedDownloadUrl(regionName, bucket, targetUri, DEFAULT_PRESIGN_EXPIRES, null, null);
  }

  public static String getPresignedDownloadUrl(String regionName, String bucket, String targetUri) {
    return getPresignedDownloadUrl(regionName, bucket, targetUri, DEFAULT_PRESIGN_EXPIRES, null, null);
  }

  public static String getPresignedDownloadUrl(String regionName, String bucket, String targetUri,
      String downloadFilename, String contentType) {
    return getPresignedDownloadUrl(regionName, bucket, targetUri, DEFAULT_PRESIGN_EXPIRES, downloadFilename,
        contentType);
  }

  public static String getPresignedDownloadUrl(String regionName, String bucket, String targetUri,
      String downloadFilename) {
    String suffix = FilenameUtils.getSuffix(downloadFilename);
    String contentType = ContentTypeUtils.getContentType(suffix);
    return getPresignedDownloadUrl(regionName, bucket, targetUri, DEFAULT_PRESIGN_EXPIRES, downloadFilename,
        contentType);
  }

  /**
   * @param bucket  bucket name
   * @param key     object key (targetUri/targetName)
   * @param expires 过期时间（S3 限制：最大 7 天）
   * @param downloadFilename 让浏览器下载时显示的文件名（可选）
   * @param contentType      响应 Content-Type（可选）
   */
  public static String getPresignedDownloadUrl(String regionName, String bucket, String key, Duration expires,
      String downloadFilename, String contentType) {
    if (expires == null) {
      expires = DEFAULT_PRESIGN_EXPIRES;
    }

    try (S3Presigner presigner = buildPresigner(regionName)) {

      GetObjectRequest.Builder getReqBuilder = GetObjectRequest.builder().bucket(bucket).key(key);

      if (downloadFilename != null && downloadFilename.length() > 0) {
        // 同时兼容普通 filename 与 RFC5987 filename*
        String safe = downloadFilename.replace("\"", "");
        String encoded = URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = "attachment; filename=\"" + safe + "\"; filename*=UTF-8''" + encoded;
        getReqBuilder.responseContentDisposition(disposition);
      } else {
        getReqBuilder.responseContentDisposition("attachment");
      }

      if (contentType != null && contentType.length() > 0) {
        getReqBuilder.responseContentType(contentType);
      }

      GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder().signatureDuration(expires)
          .getObjectRequest(getReqBuilder.build()).build();

      PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
      return presigned.url().toString();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // -------------------------
  // Client / Presigner builders
  // -------------------------
  public static S3Client buildClient() {
    S3ClientBuilder builder = S3Client.builder();

    Region region = Region.of(regionName);
    builder.region(region);

    AwsCredentialsProvider credentialsProvider = resolveCredentialsProvider();

    return builder.credentialsProvider(credentialsProvider).build();
  }

  public static S3Presigner buildPresigner() {
    return buildPresigner(regionName);
  }

  public static S3Presigner buildPresigner(String regionName) {
    Region region = Region.of(regionName);
    AwsCredentialsProvider credentialsProvider = resolveCredentialsProvider();

    return S3Presigner.builder().region(region).credentialsProvider(credentialsProvider).build();
  }

  private static AwsCredentialsProvider resolveCredentialsProvider() {
    AwsCredentialsProvider credentialsProvider;
    if (accessKeyId != null && secretAccessKey != null) {
      AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
      credentialsProvider = StaticCredentialsProvider.create(awsCreds);
    } else if (AWS_PROFILE != null) {
      credentialsProvider = ProfileCredentialsProvider.create(AWS_PROFILE);
    } else {
      credentialsProvider = DefaultCredentialsProvider.create();
    }
    return credentialsProvider;
  }

  public static String getBucketName() {
    return bucketName;
  }

  public static String getRegionName() {
    return regionName;
  }

}