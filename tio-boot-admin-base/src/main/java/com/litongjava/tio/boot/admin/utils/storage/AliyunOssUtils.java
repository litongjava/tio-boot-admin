package com.litongjava.tio.boot.admin.utils.storage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyun.oss.model.ResponseHeaderOverrides;

import nexus.io.tio.utils.environment.EnvUtils;
import nexus.io.tio.utils.http.ContentTypeUtils;
import nexus.io.tio.utils.hutool.FilenameUtils;

/**
 * Aliyun OSS 工具类（对齐 AwsS3Utils 的能力）
 * - upload（byte[] / File）
 * - getUrl（公开 URL，仅当 bucket/object 允许公开访问或走自定义域名/CDN）
 * - getPresignedDownloadUrl（私有桶可下载：预签名 GET URL）
 * - buildClient（统一凭证/region/endpoint 构建）
 */
public class AliyunOssUtils {

  /**
   * URL 模板: https://<bucket>.oss-<region>.aliyuncs.com/<objectKey>
   * 说明：regionName 一般是类似 cn-hangzhou；endpoint 一般是 https://oss-cn-hangzhou.aliyuncs.com
   */
  public static final String urlFormat = "https://%s.oss-%s.aliyuncs.com/%s";

  // Config
  public static final String bucketName = EnvUtils.get("OSS_BUCKET_NAME");
  public static final String regionName = EnvUtils.get("OSS_REGION_NAME"); // e.g. cn-hangzhou
  public static final String endpoint = EnvUtils.get("OSS_ENDPOINT"); // e.g. https://oss-cn-hangzhou.aliyuncs.com
  public static final String accessKeyId = EnvUtils.get("OSS_ACCESS_KEY_ID");
  public static final String accessKeySecret = EnvUtils.get("OSS_ACCESS_KEY_SECRET");

  /** 可选：自定义域名 / CDN 域名（和 AwsS3Utils 的 domain 对齐） */
  public static final String domain = EnvUtils.getStr("OSS_BUCKET_DOMAIN"); // e.g. cdn.example.com

  /** 默认预签名有效期（可按需调整） */
  public static final Duration DEFAULT_PRESIGN_EXPIRES = Duration.ofMinutes(30);

  // -------------------------
  // Upload
  // -------------------------

  public static PutObjectResult upload(OSS client, String targetName, byte[] fileContent, String suffix) {
    return upload(client, bucketName, targetName, fileContent, suffix);
  }

  public static PutObjectResult upload(OSS client, String bucketName, String objectKey, byte[] bytes, String suffix) {
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(bytes.length);
      if (suffix != null) {
        metadata.setContentType(ContentTypeUtils.getContentType(suffix));
      }

      PutObjectRequest req = new PutObjectRequest(bucketName, objectKey, new ByteArrayInputStream(bytes), metadata);
      return client.putObject(req);
    } catch (Exception e) {
      throw new RuntimeException("Aliyun OSS upload error", e);
    }
  }

  public static PutObjectResult upload(OSS client, String objectKey, File file) {
    return upload(client, bucketName, objectKey, file);
  }

  public static PutObjectResult upload(OSS client, String bucketName, String objectKey, File file) {
    String name = file.getName();
    long length = file.length();
    String suffix = FilenameUtils.getSuffix(name);
    String contentType = ContentTypeUtils.getContentType(suffix);
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(length);
      if (suffix != null) {
        metadata.setContentType(contentType);
      }

      PutObjectRequest req = new PutObjectRequest(bucketName, objectKey, file, metadata);
      return client.putObject(req);
    } catch (Exception e) {
      throw new RuntimeException("Aliyun OSS upload error", e);
    }
  }

  // -------------------------
  // Public URL (only works if bucket/object is public or via domain/CDN)
  // -------------------------
  public static String getUrl(String objectKey) {
    return getUrl(bucketName, objectKey);
  }

  public static String getUrl(String bucket, String objectKey) {
    if (domain != null) {
      return "https://" + domain + "/" + objectKey;
    } else {
      return String.format(urlFormat, bucket, regionName, objectKey);
    }
  }

  public static String getUrl(String regionName, String bucket, String objectKey) {
    if (domain != null) {
      return "https://" + domain + "/" + objectKey;
    } else {
      return String.format(urlFormat, bucket, regionName, objectKey);
    }
  }

  // -------------------------
  // Presigned Download URL (works for private bucket)
  // -------------------------
  /**
   * 生成可下载的预签名 GET URL（默认 30 分钟）
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

  /**
   * 生成可下载的预签名 URL（GET）。
   *
   * @param bucket           bucket name
   * @param objectKey        object key（你的 targetName/targetUri）
   * @param expires          过期时间
   * @param downloadFilename 下载保存时显示的文件名（可选）
   * @param contentType      响应 Content-Type（可选）
   */
  public static String getPresignedDownloadUrl(String bucket, String objectKey, Duration expires,
      String downloadFilename, String contentType) {
    return getPresignedDownloadUrl(regionName, bucket, objectKey, expires, downloadFilename, contentType);

  }

  public static String getPresignedDownloadUrl(String regionName, String bucket, String targetUri,
      String downloadFilename) {
    String suffix = FilenameUtils.getSuffix(downloadFilename);
    String contentType = ContentTypeUtils.getContentType(suffix);
    return getPresignedDownloadUrl(regionName, bucket, targetUri, DEFAULT_PRESIGN_EXPIRES, downloadFilename,
        contentType);
  }

  public static String getPresignedDownloadUrl(String region, String bucket, String objectKey, Duration expires,
      String downloadFilename, String contentType) {

    if (expires == null) {
      expires = DEFAULT_PRESIGN_EXPIRES;
    }

    Date expiration = new Date(System.currentTimeMillis() + expires.toMillis());

    OSS client = null;
    try {
      client = buildClient(region);

      GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, objectKey, HttpMethod.GET);
      req.setExpiration(expiration);

      // 设置响应头覆盖：Content-Disposition / Content-Type
      ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();

      if (downloadFilename != null && downloadFilename.length() > 0) {
        String safe = downloadFilename.replace("\"", "");
        String encoded = URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = "attachment; filename=\"" + safe + "\"; filename*=UTF-8''" + encoded;
        overrides.setContentDisposition(disposition);
      } else {
        overrides.setContentDisposition("attachment");
      }

      if (contentType != null && contentType.length() > 0) {
        overrides.setContentType(contentType);
      }

      req.setResponseHeaders(overrides);

      URL url = client.generatePresignedUrl(req);
      return url.toString();

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      if (client != null) {
        client.shutdown();
      }
    }
  }

  public static OSS buildClient() {
    return buildClient(regionName);
  }

  // -------------------------
  // Client builder
  // -------------------------
  public static OSS buildClient(String regionName) {
    try {
      ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
      conf.setSignatureVersion(SignVersion.V4);

      DefaultCredentialProvider credentialProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

      return OSSClientBuilder.create().endpoint(endpoint).credentialsProvider(credentialProvider)
          .clientConfiguration(conf).region(regionName).build();

    } catch (Exception e) {
      throw new RuntimeException("Failed to build Aliyun OSS client", e);
    }
  }

  public static String getBucketName() {
    return bucketName;
  }

  public static String getRegionName() {
    return regionName;
  }

}