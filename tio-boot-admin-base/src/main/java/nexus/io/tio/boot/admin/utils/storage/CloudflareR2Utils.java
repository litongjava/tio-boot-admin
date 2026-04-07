package nexus.io.tio.boot.admin.utils.storage;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import nexus.io.tio.utils.environment.EnvUtils;
import nexus.io.tio.utils.http.ContentTypeUtils;
import nexus.io.tio.utils.hutool.FilenameUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

/**
 * Cloudflare R2 工具类：S3 兼容，方法风格对齐 AwsS3Utils
 *
 * 建议环境变量：
 * - R2_BUCKET_NAME
 * - R2_ACCESS_KEY_ID
 * - R2_SECRET_ACCESS_KEY
 * - R2_ACCOUNT_ID（可选：不传则用 R2_ENDPOINT）
 * - R2_ENDPOINT（可选：形如 https://<accountid>.r2.cloudflarestorage.com）
 * - R2_REGION（可选：建议 "auto"）
 * - R2_BUCKET_DOMAIN（可选：公开访问域名/CDN 域名）
 */
public class CloudflareR2Utils {

  /**
   * 若你有自己的公开域名（CDN/自定义域名），优先用它拼接公开 URL
   */
  public static final String domain = EnvUtils.getStr("R2_BUCKET_DOMAIN");

  public static final String bucketName = EnvUtils.getStr("R2_BUCKET_NAME");
  public static final String accessKeyId = EnvUtils.getStr("R2_ACCESS_KEY_ID");
  public static final String secretAccessKey = EnvUtils.getStr("R2_SECRET_ACCESS_KEY");

  public static final String accountId = EnvUtils.getStr("R2_ACCOUNT_ID");
  public static final String endpoint = EnvUtils.getStr("R2_ENDPOINT");
  public static final String regionName = EnvUtils.getStr("R2_REGION");

  public static final Duration DEFAULT_PRESIGN_EXPIRES = Duration.ofMinutes(30);

  private static final String DEFAULT_REGION = resolveRegion(regionName);
  private static final String DEFAULT_ENDPOINT = resolveEndpoint();

  private static final AwsCredentialsProvider CREDENTIALS_PROVIDER = resolveCredentialsProvider();

  private static final S3Client S3_CLIENT = createClient(DEFAULT_REGION, DEFAULT_ENDPOINT);
  private static final S3Presigner PRESIGNER = createPresigner(DEFAULT_REGION, DEFAULT_ENDPOINT);

  // -------------------------
  // Upload
  // -------------------------

  public static PutObjectResponse upload(String targetName, byte[] fileContent, String suffix) {
    return upload(bucketName, targetName, fileContent, suffix);
  }

  public static PutObjectResponse upload(String bucketName, String targetName, byte[] fileContent, String suffix) {
    try {
      String contentType = ContentTypeUtils.getContentType(suffix);
      PutObjectRequest putOb = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(targetName)
          .contentType(contentType)
          .build();

      return S3_CLIENT.putObject(putOb, RequestBody.fromBytes(fileContent));
    } catch (Exception e) {
      throw new RuntimeException("Cloudflare R2 upload failed, bucket=" + bucketName + ", key=" + targetName, e);
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
      PutObjectRequest putOb = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(targetName)
          .contentType(contentType)
          .build();

      return S3_CLIENT.putObject(putOb, RequestBody.fromFile(file));
    } catch (Exception e) {
      throw new RuntimeException("Cloudflare R2 upload failed, bucket=" + bucketName + ", key=" + targetName, e);
    }
  }

  // -------------------------
  // Public URL (仅当对象公开/域名放行时可用)
  // -------------------------

  public static String getUrl(String targetUri) {
    return getUrl(bucketName, targetUri);
  }

  public static String getUrl(String bucketName, String targetUri) {
    if (isNotBlank(domain)) {
      return "https://" + domain + "/" + targetUri;
    }

    String base = DEFAULT_ENDPOINT.endsWith("/") ? DEFAULT_ENDPOINT.substring(0, DEFAULT_ENDPOINT.length() - 1)
        : DEFAULT_ENDPOINT;
    return base + "/" + bucketName + "/" + targetUri;
  }

  public static String getUrl(String regionName, String bucketName, String targetUri) {
    if (isNotBlank(domain)) {
      return "https://" + domain + "/" + targetUri;
    }

    String ep = resolveEndpoint();
    String base = ep.endsWith("/") ? ep.substring(0, ep.length() - 1) : ep;
    return base + "/" + bucketName + "/" + targetUri;
  }

  // -------------------------
  // Presigned Download URL (私有 bucket 推荐用这个)
  // -------------------------

  public static String getPresignedDownloadUrl(String targetUri) {
    return getPresignedDownloadUrl(DEFAULT_REGION, bucketName, targetUri, DEFAULT_PRESIGN_EXPIRES, null, null);
  }

  public static String getPresignedDownloadUrl(String bucket, String targetUri) {
    return getPresignedDownloadUrl(DEFAULT_REGION, bucket, targetUri, DEFAULT_PRESIGN_EXPIRES, null, null);
  }

  public static String getPresignedDownloadUrl(String regionName, String bucket, String targetUri) {
    return getPresignedDownloadUrl(regionName, bucket, targetUri, DEFAULT_PRESIGN_EXPIRES, null, null);
  }

  public static String getPresignedDownloadUrl(String regionName, String bucket, String targetUri,
      String downloadFilename) {
    String suffix = FilenameUtils.getSuffix(downloadFilename);
    String contentType = ContentTypeUtils.getContentType(suffix);
    return getPresignedDownloadUrl(regionName, bucket, targetUri, DEFAULT_PRESIGN_EXPIRES, downloadFilename,
        contentType);
  }

  public static String getPresignedDownloadUrl(String bucket, String key, Duration expires, String downloadFilename,
      String contentType) {
    return getPresignedDownloadUrl(DEFAULT_REGION, bucket, key, expires, downloadFilename, contentType);
  }

  /**
   * 生成可下载的预签名 GET URL
   *
   * @param regionName region
   * @param bucket bucket name
   * @param key object key
   * @param expires 过期时间
   * @param downloadFilename 下载保存的文件名（可选）
   * @param contentType 响应 Content-Type（可选）
   */
  public static String getPresignedDownloadUrl(String regionName, String bucket, String key, Duration expires,
      String downloadFilename, String contentType) {

    if (expires == null) {
      expires = DEFAULT_PRESIGN_EXPIRES;
    }

    String resolvedRegion = resolveRegion(regionName);
    String resolvedEndpoint = resolveEndpoint();

    S3Presigner presigner = null;
    boolean shouldClose = false;

    try {
      if (DEFAULT_REGION.equals(resolvedRegion) && DEFAULT_ENDPOINT.equals(resolvedEndpoint)) {
        presigner = PRESIGNER;
      } else {
        presigner = createPresigner(resolvedRegion, resolvedEndpoint);
        shouldClose = true;
      }

      GetObjectRequest.Builder getReq = GetObjectRequest.builder()
          .bucket(bucket)
          .key(key);

      if (isNotBlank(downloadFilename)) {
        String safe = downloadFilename.replace("\"", "");
        String encoded = URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = "attachment; filename=\"" + safe + "\"; filename*=UTF-8''" + encoded;
        getReq.responseContentDisposition(disposition);
      } else {
        getReq.responseContentDisposition("attachment");
      }

      if (isNotBlank(contentType)) {
        getReq.responseContentType(contentType);
      }

      GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
          .signatureDuration(expires)
          .getObjectRequest(getReq.build())
          .build();

      PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
      return presigned.url().toString();

    } catch (Exception e) {
      throw new RuntimeException(
          "Generate Cloudflare R2 presigned download url failed, region=" + resolvedRegion + ", bucket=" + bucket
              + ", key=" + key,
          e);
    } finally {
      if (shouldClose && presigner != null) {
        try {
          presigner.close();
        } catch (Exception ignored) {
        }
      }
    }
  }

  // -------------------------
  // Client / Presigner builders
  // -------------------------

  public static S3Client buildClient() {
    return S3_CLIENT;
  }

  public static S3Client buildClient(String regionName) {
    String resolvedRegion = resolveRegion(regionName);
    if (DEFAULT_REGION.equals(resolvedRegion)) {
      return S3_CLIENT;
    }
    return createClient(resolvedRegion, DEFAULT_ENDPOINT);
  }

  public static S3Presigner buildPresigner() {
    return PRESIGNER;
  }

  public static S3Presigner buildPresigner(String regionName) {
    String resolvedRegion = resolveRegion(regionName);
    if (DEFAULT_REGION.equals(resolvedRegion)) {
      return PRESIGNER;
    }
    return createPresigner(resolvedRegion, DEFAULT_ENDPOINT);
  }

  private static S3Client createClient(String regionName, String endpoint) {
    validateConfig();

    S3ClientBuilder builder = S3Client.builder();

    builder.region(Region.of(regionName));
    builder.endpointOverride(URI.create(endpoint));
    builder.credentialsProvider(CREDENTIALS_PROVIDER);

    builder.serviceConfiguration(
        S3Configuration.builder()
            .pathStyleAccessEnabled(true)
            .chunkedEncodingEnabled(false)
            .build());

    builder.httpClient(ApacheHttpClient.builder().build());

    return builder.build();
  }

  private static S3Presigner createPresigner(String regionName, String endpoint) {
    validateConfig();

    return S3Presigner.builder()
        .region(Region.of(regionName))
        .endpointOverride(URI.create(endpoint))
        .credentialsProvider(CREDENTIALS_PROVIDER)
        .build();
  }

  private static AwsCredentialsProvider resolveCredentialsProvider() {
    validateConfig();
    AwsBasicCredentials creds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    return StaticCredentialsProvider.create(creds);
  }

  private static String resolveEndpoint() {
    if (isNotBlank(endpoint)) {
      return endpoint;
    }
    if (isNotBlank(accountId)) {
      return "https://" + accountId + ".r2.cloudflarestorage.com";
    }
    throw new IllegalStateException("R2_ENDPOINT or R2_ACCOUNT_ID is empty");
  }

  private static String resolveRegion(String regionName) {
    if (isNotBlank(regionName)) {
      return regionName;
    }
    return "auto";
  }

  private static void validateConfig() {
    if (!isNotBlank(bucketName)) {
      throw new IllegalStateException("R2_BUCKET_NAME is empty");
    }
    if (!isNotBlank(accessKeyId) || !isNotBlank(secretAccessKey)) {
      throw new IllegalStateException("R2_ACCESS_KEY_ID / R2_SECRET_ACCESS_KEY is empty");
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