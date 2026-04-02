package com.litongjava.tio.boot.admin.utils.storage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.SignUrlOption;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.cloud.StorageClient;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.ContentTypeUtils;
import com.litongjava.tio.utils.hutool.FilenameUtils;

public class GoogleStorageUtils {

  /**
   * Firebase / GCS bucket name 例如: xxx.appspot.com 或 xxx.firebasestorage.app
   */
  public static final String bucketName = EnvUtils.get("GCS_BUCKET_NAME", EnvUtils.get("BUCKET_NAME"));

  /**
   * GCP region，主要为了和 AwsS3Utils 风格保持一致。 GCS 实际生成公共 URL 时通常不需要 region。
   */
  public static final String regionName = EnvUtils.getStr("GCS_REGION_NAME", "auto");

  /**
   * 自定义域名，可选
   */
  public static final String domain = EnvUtils.getStr("GCS_BUCKET_DOMAIN");

  /**
   * 默认预签名有效期：30分钟
   */
  public static final long DEFAULT_PRESIGN_EXPIRES_MINUTES = 30L;

  // -------------------------
  // Upload
  // -------------------------

  public static String upload(String targetName, byte[] fileContent, String suffix) {
    return upload(bucketName, targetName, fileContent, suffix);
  }

  public static String upload(String bucketName, String targetName, byte[] fileContent, String suffix) {
    try {
      String contentType = ContentTypeUtils.getContentType(suffix);

      BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, targetName).setContentType(contentType).build();

      Blob blob = buildStorage().create(blobInfo, fileContent);
      return blob.getEtag();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // -------------------------
  // Public URL
  // -------------------------

  public static String getUrl(String targetUri) {
    return getUrl(bucketName, targetUri);
  }

  public static String getUrl(String bucketName, String targetUri) {
    if (domain != null && domain.length() > 0) {
      return "https://" + domain + "/" + targetUri;
    }

    // 推荐使用 storage.googleapis.com 直链
    return String.format("https://storage.googleapis.com/%s/%s", bucketName, encodePath(targetUri));
  }

  public static String getUrl(String regionName, String bucketName, String targetUri) {
    return getUrl(bucketName, targetUri);
  }

  // -------------------------
  // Presigned Download URL
  // -------------------------

  public static String getPresignedDownloadUrl(String targetUri) {
    return getPresignedDownloadUrl(regionName, bucketName, targetUri, null, null);
  }

  public static String getPresignedDownloadUrl(String bucket, String targetUri) {
    return getPresignedDownloadUrl(regionName, bucket, targetUri, null, null);
  }

  public static String getPresignedDownloadUrl(String regionName, String bucket, String targetUri) {
    return getPresignedDownloadUrl(regionName, bucket, targetUri, null, null);
  }

  public static String getPresignedDownloadUrl(String regionName, String bucket, String targetUri,
      String downloadFilename) {
    String suffix = FilenameUtils.getSuffix(downloadFilename);
    String contentType = ContentTypeUtils.getContentType(suffix);
    return getPresignedDownloadUrl(regionName, bucket, targetUri, downloadFilename, contentType);
  }

  public static String getPresignedDownloadUrl(String regionName, String bucket, String targetUri,
      String downloadFilename, String contentType) {
    try {
      Storage storage = buildStorage();

      BlobInfo blobInfo = BlobInfo.newBuilder(bucket, targetUri).build();

      SignUrlOption[] options;
      if (downloadFilename != null && downloadFilename.length() > 0) {
        String safe = downloadFilename.replace("\"", "");
        String encoded = URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = "attachment; filename=\"" + safe + "\"; filename*=UTF-8''" + encoded;

        if (contentType != null && contentType.length() > 0) {
          options = new SignUrlOption[] { SignUrlOption.httpMethod(HttpMethod.GET), SignUrlOption.withV4Signature(),
              SignUrlOption.withExtHeaders(java.util.Map.of("response-content-disposition", disposition,
                  "response-content-type", contentType)) };
        } else {
          options = new SignUrlOption[] { SignUrlOption.httpMethod(HttpMethod.GET), SignUrlOption.withV4Signature(),
              SignUrlOption.withExtHeaders(java.util.Map.of("response-content-disposition", disposition)) };
        }
      } else {
        options = new SignUrlOption[] { SignUrlOption.httpMethod(HttpMethod.GET), SignUrlOption.withV4Signature() };
      }

      return storage.signUrl(blobInfo, DEFAULT_PRESIGN_EXPIRES_MINUTES, TimeUnit.MINUTES, options).toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // -------------------------
  // Storage builder
  // -------------------------

  public static Storage buildStorage() {
    try {
      // 优先使用 Firebase Admin 初始化后的 Storage
      try {
        return StorageClient.getInstance().bucket().getStorage();
      } catch (Exception e) {
        // 忽略，走默认 GCP 凭证
      }

      GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
      return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String getBucketName() {
    return bucketName;
  }

  public static String getRegionName() {
    return regionName;
  }

  private static String encodePath(String path) {
    return path.replace(" ", "%20");
  }
}