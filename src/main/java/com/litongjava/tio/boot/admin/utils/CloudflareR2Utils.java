package com.litongjava.tio.boot.admin.utils;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.ContentTypeUtils;
import com.litongjava.tio.utils.hutool.FilenameUtils;

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

  // 若你有自己的公开域名（CDN/自定义域名），优先用它拼接公开 URL
  public static final String domain = EnvUtils.getStr("R2_BUCKET_DOMAIN");

  public static final String bucketName = EnvUtils.getStr("R2_BUCKET_NAME");
  public static final String accessKeyId = EnvUtils.getStr("R2_ACCESS_KEY_ID");
  public static final String secretAccessKey = EnvUtils.getStr("R2_SECRET_ACCESS_KEY");

  public static final String accountId = EnvUtils.getStr("R2_ACCOUNT_ID");
  public static final String endpoint = EnvUtils.getStr("R2_ENDPOINT"); // 可直接配置完整 endpoint
  public static final String regionName = EnvUtils.getStr("R2_REGION"); // 建议 auto

  public static final Duration DEFAULT_PRESIGN_EXPIRES = Duration.ofMinutes(30);

  // -------------------------
  // Upload
  // -------------------------

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
  // Public URL (仅当对象公开/域名放行时可用)
  // -------------------------

  public static String getUrl(String targetUri) {
    return getUrl(bucketName, targetUri);
  }

  public static String getUrl(String bucketName, String targetUri) {
    if (domain != null && domain.length() > 0) {
      return "https://" + domain + "/" + targetUri;
    }
    // R2 默认 endpoint 不包含 bucket 子域名；如果你没配置 domain，返回 endpoint + /bucket/key 这种路径形式
    // 注意：该 URL 不一定“公开可访问”，仅作为展示/记录用；私有桶请用预签名 URL
    String ep = resolveEndpoint();
    String base = ep.endsWith("/") ? ep.substring(0, ep.length() - 1) : ep;
    return base + "/" + bucketName + "/" + targetUri;
  }

  // -------------------------
  // Presigned Download URL (私有 bucket 推荐用这个)
  // -------------------------

  public static String getPresignedDownloadUrl(String targetUri) {
    return getPresignedDownloadUrl(bucketName, targetUri, DEFAULT_PRESIGN_EXPIRES, null, null);
  }

  public static String getPresignedDownloadUrl(String bucket, String targetUri) {
    return getPresignedDownloadUrl(bucket, targetUri, DEFAULT_PRESIGN_EXPIRES, null, null);
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
    String region = resolveRegion();
    return getPresignedDownloadUrl(region, bucket, key, expires, downloadFilename, contentType);
  }

  /**
   * 生成可下载的预签名 GET URL
   *
   * @param bucket           bucket name
   * @param key              object key
   * @param expires          过期时间
   * @param downloadFilename 下载保存的文件名（可选）
   * @param contentType      响应 Content-Type（可选）
   */
  public static String getPresignedDownloadUrl(String regionName, String bucket, String key, Duration expires,
      String downloadFilename, String contentType) {

    if (expires == null) {
      expires = DEFAULT_PRESIGN_EXPIRES;
    }

    try (S3Presigner presigner = buildPresigner(regionName)) {

      GetObjectRequest.Builder getReq = GetObjectRequest.builder().bucket(bucket).key(key);

      if (downloadFilename != null && downloadFilename.length() > 0) {
        String safe = downloadFilename.replace("\"", "");
        String encoded = URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = "attachment; filename=\"" + safe + "\"; filename*=UTF-8''" + encoded;
        getReq.responseContentDisposition(disposition);
      } else {
        getReq.responseContentDisposition("attachment");
      }

      if (contentType != null && contentType.length() > 0) {
        getReq.responseContentType(contentType);
      }

      GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder().signatureDuration(expires)
          .getObjectRequest(getReq.build()).build();

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
    validateConfig();

    S3ClientBuilder builder = S3Client.builder();

    builder.region(Region.of(resolveRegion()));
    builder.endpointOverride(URI.create(resolveEndpoint()));
    builder.credentialsProvider(resolveCredentialsProvider());

    // R2 常见建议：path-style + 关闭 chunked encoding
    builder.serviceConfiguration(
        S3Configuration.builder().pathStyleAccessEnabled(true).chunkedEncodingEnabled(false).build());

    // 显式指定 HTTP client（可选，但更可控）
    builder.httpClient(ApacheHttpClient.builder().build());

    return builder.build();
  }

  public static S3Presigner buildPresigner() {
    String region = resolveRegion();
    return buildPresigner(region);
  }

  public static S3Presigner buildPresigner(String regionName) {
    validateConfig();

    return S3Presigner.builder().region(Region.of(regionName)).endpointOverride(URI.create(resolveEndpoint()))
        .credentialsProvider(resolveCredentialsProvider()).build();
  }

  private static AwsCredentialsProvider resolveCredentialsProvider() {
    AwsBasicCredentials creds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
    return StaticCredentialsProvider.create(creds);
  }

  private static String resolveEndpoint() {
    if (endpoint != null && endpoint.length() > 0) {
      return endpoint;
    }
    if (accountId != null && accountId.length() > 0) {
      return "https://" + accountId + ".r2.cloudflarestorage.com";
    }
    throw new IllegalStateException("R2_ENDPOINT or R2_ACCOUNT_ID is empty");
  }

  private static String resolveRegion() {
    if (regionName != null && regionName.length() > 0) {
      return regionName;
    }
    // R2 常用 region = "auto"
    return "auto";
  }

  private static void validateConfig() {
    if (bucketName == null || bucketName.length() == 0) {
      throw new IllegalStateException("R2_BUCKET_NAME is empty");
    }
    if (accessKeyId == null || accessKeyId.length() == 0 || secretAccessKey == null || secretAccessKey.length() == 0) {
      throw new IllegalStateException("R2_ACCESS_KEY_ID / R2_SECRET_ACCESS_KEY is empty");
    }
    // endpoint/accountId 在 resolveEndpoint() 里校验
  }
}