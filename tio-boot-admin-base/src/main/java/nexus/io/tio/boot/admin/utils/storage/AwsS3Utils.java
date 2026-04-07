package nexus.io.tio.boot.admin.utils.storage;

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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * Tong Li
 */
public class AwsS3Utils {

  public static final String urlFormat = "https://%s.s3.%s.amazonaws.com/%s";

  public static final String bucketName = EnvUtils.get("AWS_S3_BUCKET_NAME");
  public static final String regionName = EnvUtils.get("AWS_S3_REGION_NAME");
  public static final String accessKeyId = EnvUtils.get("AWS_S3_ACCESS_KEY_ID");
  public static final String secretAccessKey = EnvUtils.get("AWS_S3_SECRET_ACCESS_KEY");
  public static final String domain = EnvUtils.getStr("AWS_S3_BUCKET_DOMAIN");
  public static final String AWS_PROFILE = EnvUtils.getStr("AWS_PROFILE");

  public static final Duration DEFAULT_PRESIGN_EXPIRES = Duration.ofMinutes(30);

  private static final Region REGION = Region.of(regionName);
  private static final AwsCredentialsProvider CREDENTIALS_PROVIDER = resolveCredentialsProvider();

  private static final S3Client S3_CLIENT = S3Client.builder().region(REGION).credentialsProvider(CREDENTIALS_PROVIDER)
      .build();

  private static final S3Presigner PRESIGNER = S3Presigner.builder().region(REGION)
      .credentialsProvider(CREDENTIALS_PROVIDER).build();

  // -------------------------
  // Upload
  // -------------------------

  public static PutObjectResponse upload(String targetName, byte[] fileContent, String suffix) {
    return upload(bucketName, targetName, fileContent, suffix);
  }

  public static PutObjectResponse upload(String bucketName, String targetName, byte[] fileContent, String suffix) {
    try {
      String contentType = ContentTypeUtils.getContentType(suffix);
      PutObjectRequest putOb = PutObjectRequest.builder().bucket(bucketName).key(targetName).contentType(contentType)
          .build();

      return S3_CLIENT.putObject(putOb, RequestBody.fromBytes(fileContent));
    } catch (Exception e) {
      throw new RuntimeException("S3 upload failed, bucket=" + bucketName + ", key=" + targetName, e);
    }
  }

  public static PutObjectResponse upload(String targetName, File file) {
    return upload(bucketName, targetName, file);
  }

  public static PutObjectResponse upload(String bucketName, String targetName, File file) {
    String name = file.getName();
    String suffix = FilenameUtils.getSuffix(name);
    String contentType = ContentTypeUtils.getContentType(suffix);

    try {
      PutObjectRequest putOb = PutObjectRequest.builder().bucket(bucketName).key(targetName).contentType(contentType)
          .build();

      return S3_CLIENT.putObject(putOb, RequestBody.fromFile(file));
    } catch (Exception e) {
      throw new RuntimeException("S3 upload failed, bucket=" + bucketName + ", key=" + targetName, e);
    }
  }

  // -------------------------
  // Public URL
  // -------------------------

  public static String getUrl(String targetUri) {
    if (isNotBlank(domain)) {
      return "https://" + domain + "/" + targetUri;
    } else {
      return String.format(urlFormat, bucketName, regionName, targetUri);
    }
  }

  public static String getUrl(String bucketName, String targetUri) {
    if (isNotBlank(domain)) {
      return "https://" + domain + "/" + targetUri;
    } else {
      return String.format(urlFormat, bucketName, regionName, targetUri);
    }
  }

  public static String getUrl(String regionName, String bucketName, String targetUri) {
    if (isNotBlank(domain)) {
      return "https://" + domain + "/" + targetUri;
    } else {
      return String.format(urlFormat, bucketName, regionName, targetUri);
    }
  }

  // -------------------------
  // Presigned Download URL
  // -------------------------

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
   * @param regionName       AWS region
   * @param bucket           bucket name
   * @param key              object key
   * @param expires          过期时间（S3 限制最大 7 天）
   * @param downloadFilename 下载文件名，可选
   * @param contentType      响应 Content-Type，可选
   */
  public static String getPresignedDownloadUrl(String regionName, String bucket, String key, Duration expires,
      String downloadFilename, String contentType) {
    if (expires == null) {
      expires = DEFAULT_PRESIGN_EXPIRES;
    }

    try {
      GetObjectRequest.Builder getReqBuilder = GetObjectRequest.builder().bucket(bucket).key(key);

      if (isNotBlank(downloadFilename)) {
        String safe = downloadFilename.replace("\"", "");
        String encoded = URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = "attachment; filename=\"" + safe + "\"; filename*=UTF-8''" + encoded;
        getReqBuilder.responseContentDisposition(disposition);
      } else {
        getReqBuilder.responseContentDisposition("attachment");
      }

      if (isNotBlank(contentType)) {
        getReqBuilder.responseContentType(contentType);
      }

      GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder().signatureDuration(expires)
          .getObjectRequest(getReqBuilder.build()).build();

      PresignedGetObjectRequest presigned = PRESIGNER.presignGetObject(presignRequest);
      return presigned.url().toString();
    } catch (Exception e) {
      throw new RuntimeException(
          "Generate presigned download url failed, region=" + regionName + ", bucket=" + bucket + ", key=" + key, e);
    }
  }

  // -------------------------
  // Client / Presigner builders
  // -------------------------

  public static S3Client buildClient() {
    return S3_CLIENT;
  }

  public static S3Presigner buildPresigner() {
    return PRESIGNER;
  }

  public static S3Presigner buildPresigner(String regionName) {
    if (AwsS3Utils.regionName.equals(regionName)) {
      return PRESIGNER;
    }

    return S3Presigner.builder().region(Region.of(regionName)).credentialsProvider(CREDENTIALS_PROVIDER).build();
  }

  private static AwsCredentialsProvider resolveCredentialsProvider() {
    if (isNotBlank(accessKeyId) && isNotBlank(secretAccessKey)) {
      AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
      return StaticCredentialsProvider.create(awsCreds);
    } else if (isNotBlank(AWS_PROFILE)) {
      return ProfileCredentialsProvider.create(AWS_PROFILE);
    } else {
      return DefaultCredentialsProvider.create();
    }
  }

  private static boolean isNotBlank(String str) {
    return str != null && !str.trim().isEmpty();
  }

  public static String getBucketName() {
    return bucketName;
  }

  public static String getRegionName() {
    return regionName;
  }

  public static void shutdown() {
    try {
      PRESIGNER.close();
    } catch (Exception ignored) {
    }

    try {
      S3_CLIENT.close();
    } catch (Exception ignored) {
    }
  }
}