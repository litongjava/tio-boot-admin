package nexus.io.tio.boot.admin.utils.storage;

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
import nexus.io.tio.utils.hutool.FilenameUtils;
import nexus.io.tio.utils.http.ContentTypeUtils;

/**
 * Aliyun OSS 工具类
 */
public class AliyunOssUtils {

  /**
   * URL 模板: https://<bucket>.oss-<region>.aliyuncs.com/<objectKey>
   * regionName 一般形如 cn-hangzhou
   */
  public static final String urlFormat = "https://%s.oss-%s.aliyuncs.com/%s";

  public static final String bucketName = EnvUtils.get("OSS_BUCKET_NAME");
  public static final String regionName = EnvUtils.get("OSS_REGION_NAME");
  public static final String endpoint = EnvUtils.get("OSS_ENDPOINT");
  public static final String accessKeyId = EnvUtils.get("OSS_ACCESS_KEY_ID");
  public static final String accessKeySecret = EnvUtils.get("OSS_ACCESS_KEY_SECRET");

  /**
   * 可选：自定义域名 / CDN 域名
   */
  public static final String domain = EnvUtils.getStr("OSS_BUCKET_DOMAIN");

  /**
   * 默认预签名有效期
   */
  public static final Duration DEFAULT_PRESIGN_EXPIRES = Duration.ofMinutes(30);

  private static final OSS OSS_CLIENT = createClient(regionName);

  // -------------------------
  // Upload
  // -------------------------

  public static PutObjectResult upload(String targetName, byte[] fileContent, String suffix) {
    return upload(bucketName, targetName, fileContent, suffix);
  }

  public static PutObjectResult upload(String bucketName, String objectKey, byte[] bytes, String suffix) {
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(bytes.length);

      if (isNotBlank(suffix)) {
        metadata.setContentType(ContentTypeUtils.getContentType(suffix));
      }

      PutObjectRequest req = new PutObjectRequest(bucketName, objectKey, new ByteArrayInputStream(bytes), metadata);
      return OSS_CLIENT.putObject(req);
    } catch (Exception e) {
      throw new RuntimeException("Aliyun OSS upload failed, bucket=" + bucketName + ", key=" + objectKey, e);
    }
  }

  public static PutObjectResult upload(String objectKey, File file) {
    return upload(bucketName, objectKey, file);
  }

  public static PutObjectResult upload(String bucketName, String objectKey, File file) {
    String name = file.getName();
    String suffix = FilenameUtils.getSuffix(name);
    String contentType = ContentTypeUtils.getContentType(suffix);

    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(file.length());

      if (isNotBlank(suffix)) {
        metadata.setContentType(contentType);
      }

      PutObjectRequest req = new PutObjectRequest(bucketName, objectKey, file, metadata);
      return OSS_CLIENT.putObject(req);
    } catch (Exception e) {
      throw new RuntimeException("Aliyun OSS upload failed, bucket=" + bucketName + ", key=" + objectKey, e);
    }
  }

  // -------------------------
  // Public URL
  // -------------------------

  public static String getUrl(String objectKey) {
    return getUrl(bucketName, objectKey);
  }

  public static String getUrl(String bucket, String objectKey) {
    if (isNotBlank(domain)) {
      return "https://" + domain + "/" + objectKey;
    } else {
      return String.format(urlFormat, bucket, regionName, objectKey);
    }
  }

  public static String getUrl(String regionName, String bucket, String objectKey) {
    if (isNotBlank(domain)) {
      return "https://" + domain + "/" + objectKey;
    } else {
      return String.format(urlFormat, bucket, regionName, objectKey);
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

  /**
   * 生成可下载的预签名 URL（GET）
   *
   * @param regionName OSS region
   * @param bucket bucket 名称
   * @param objectKey 对象 key
   * @param expires 过期时间
   * @param downloadFilename 下载保存时显示的文件名，可选
   * @param contentType 响应 Content-Type，可选
   */
  public static String getPresignedDownloadUrl(String regionName, String bucket, String objectKey, Duration expires,
      String downloadFilename, String contentType) {

    if (expires == null) {
      expires = DEFAULT_PRESIGN_EXPIRES;
    }

    Date expiration = new Date(System.currentTimeMillis() + expires.toMillis());

    OSS client = null;
    boolean shouldShutdown = false;

    try {
      if (AliyunOssUtils.regionName.equals(regionName)) {
        client = OSS_CLIENT;
      } else {
        client = createClient(regionName);
        shouldShutdown = true;
      }

      GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, objectKey, HttpMethod.GET);
      req.setExpiration(expiration);

      ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();

      if (isNotBlank(downloadFilename)) {
        String safe = downloadFilename.replace("\"", "");
        String encoded = URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = "attachment; filename=\"" + safe + "\"; filename*=UTF-8''" + encoded;
        overrides.setContentDisposition(disposition);
      } else {
        overrides.setContentDisposition("attachment");
      }

      if (isNotBlank(contentType)) {
        overrides.setContentType(contentType);
      }

      req.setResponseHeaders(overrides);

      URL url = client.generatePresignedUrl(req);
      return url.toString();

    } catch (Exception e) {
      throw new RuntimeException(
          "Generate Aliyun OSS presigned download url failed, region=" + regionName + ", bucket=" + bucket + ", key="
              + objectKey,
          e);
    } finally {
      if (shouldShutdown && client != null) {
        try {
          client.shutdown();
        } catch (Exception ignored) {
        }
      }
    }
  }

  // -------------------------
  // Client builder
  // -------------------------

  public static OSS buildClient() {
    return OSS_CLIENT;
  }

  public static OSS buildClient(String regionName) {
    if (AliyunOssUtils.regionName.equals(regionName)) {
      return OSS_CLIENT;
    }
    return createClient(regionName);
  }

  private static OSS createClient(String regionName) {
    if (!isNotBlank(regionName)) {
      throw new IllegalStateException("OSS_REGION_NAME is empty");
    }
    if (!isNotBlank(bucketName)) {
      throw new IllegalStateException("OSS_BUCKET_NAME is empty");
    }
    if (!isNotBlank(endpoint)) {
      throw new IllegalStateException("OSS_ENDPOINT is empty");
    }
    if (!isNotBlank(accessKeyId) || !isNotBlank(accessKeySecret)) {
      throw new IllegalStateException("OSS_ACCESS_KEY_ID / OSS_ACCESS_KEY_SECRET is empty");
    }

    try {
      ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
      conf.setSignatureVersion(SignVersion.V4);

      DefaultCredentialProvider credentialProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

      return OSSClientBuilder.create()
          .endpoint(endpoint)
          .credentialsProvider(credentialProvider)
          .clientConfiguration(conf)
          .region(regionName)
          .build();

    } catch (Exception e) {
      throw new RuntimeException("Failed to build Aliyun OSS client, region=" + regionName, e);
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
      OSS_CLIENT.shutdown();
    } catch (Exception ignored) {
    }
  }
}