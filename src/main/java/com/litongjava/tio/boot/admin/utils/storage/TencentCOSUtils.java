package com.litongjava.tio.boot.admin.utils.storage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.ContentTypeUtils;
import com.litongjava.tio.utils.hutool.FilenameUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;

/**
 * Tencent COS 工具类
 */
public class TencentCOSUtils {

  /**
   * 公网访问 URL 模板:
   * https://<bucket>.cos.<region>.myqcloud.com/<objectKey>
   */
  public static final String urlFormat = "https://%s.cos.%s.myqcloud.com/%s";

  public static final String bucketName = EnvUtils.get("TENCENT_COS_BUCKET_NAME");
  public static final String regionName = EnvUtils.get("TENCENT_COS_REGION_NAME");
  public static final String secretId = EnvUtils.get("TENCENT_COS_SECRET_ID");
  public static final String secretKey = EnvUtils.get("TENCENT_COS_SECRET_KEY");

  /**
   * 可选：自定义域名（CDN/COS 绑定域名），例如 cdn.example.com
   */
  public static final String domain = EnvUtils.getStr("TENCENT_COS_BUCKET_DOMAIN");

  /** 默认预签名有效期 */
  public static final Duration DEFAULT_PRESIGN_EXPIRES = Duration.ofMinutes(30);

  // -------------------------
  // Upload
  // -------------------------

  /**
   * 上传字节数组
   *
   * @param client    COSClient
   * @param bucket    bucket 名称（注意：腾讯 COS bucket 通常包含 appid 后缀，如 xxx-1250000000）
   * @param objectKey 对象 key
   * @param bytes     文件内容
   * @param suffix    后缀（用于推断 Content-Type）
   */
  public static PutObjectResult upload(COSClient client, String bucket, String objectKey, byte[] bytes, String suffix) {
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(bytes.length);

      if (suffix != null && suffix.length() > 0) {
        metadata.setContentType(ContentTypeUtils.getContentType(suffix));
      }

      ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
      PutObjectRequest req = new PutObjectRequest(bucket, objectKey, inputStream, metadata);
      return client.putObject(req);
    } catch (Exception e) {
      throw new RuntimeException("Tencent COS upload error", e);
    }
  }

  /**
   * 上传 File
   */
  public static PutObjectResult upload(COSClient client, String objectKey, File file) {
    return upload(client, bucketName, objectKey, file);
  }

  public static PutObjectResult upload(COSClient client, String bucket, String objectKey, File file) {
    String name = file.getName();
    String suffix = FilenameUtils.getSuffix(name);
    String contentType = ContentTypeUtils.getContentType(suffix);

    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(file.length());
      if (suffix != null && suffix.length() > 0) {
        metadata.setContentType(contentType);
      }

      PutObjectRequest req = new PutObjectRequest(bucket, objectKey, file);
      req.setMetadata(metadata);

      return client.putObject(req);
    } catch (Exception e) {
      throw new RuntimeException("Tencent COS upload error", e);
    }
  }

  // -------------------------
  // Public URL (仅当对象公开/域名放行时可用)
  // -------------------------

  public static String getUrl(String objectKey) {
    return getUrl(bucketName, objectKey);
  }

  public static String getUrl(String bucket, String objectKey) {
    if (domain != null && domain.length() > 0) {
      return "https://" + domain + "/" + objectKey;
    }
    return String.format(urlFormat, bucket, regionName, objectKey);
  }

  public static String getUrl(String regionName, String bucket, String objectKey) {
    if (domain != null && domain.length() > 0) {
      return "https://" + domain + "/" + objectKey;
    }
    return String.format(urlFormat, bucket, regionName, objectKey);
  }

  // -------------------------
  // Presigned Download URL (私有 bucket 推荐用这个)
  // -------------------------

  public static String getPresignedDownloadUrl(String objectKey) {
    return getPresignedDownloadUrl(bucketName, objectKey, DEFAULT_PRESIGN_EXPIRES, null, null);
  }

  public static String getPresignedDownloadUrl(String bucket, String objectKey) {
    return getPresignedDownloadUrl(bucket, objectKey, DEFAULT_PRESIGN_EXPIRES, null, null);
  }

  public static String getPresignedDownloadUrl(String region_name, String bucket, String targetName) {
    return getPresignedDownloadUrl(region_name, bucket, targetName, DEFAULT_PRESIGN_EXPIRES, null, null);
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
   * 生成可下载的预签名 URL（GET）。
   *
   * @param bucket           bucket 名称
   * @param objectKey        对象 key
   * @param expires          过期时间
   * @param downloadFilename 下载保存时显示的文件名（可选）
   * @param contentType      响应 Content-Type（可选）
   */
  public static String getPresignedDownloadUrl(String regionName, String bucket, String objectKey, Duration expires,
      String downloadFilename, String contentType) {

    if (expires == null) {
      expires = DEFAULT_PRESIGN_EXPIRES;
    }

    COSClient client = null;
    try {
      client = buildClient(regionName);

      Date expiration = new Date(System.currentTimeMillis() + expires.toMillis());

      GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucket, objectKey, HttpMethodName.GET);
      req.setExpiration(expiration);

      // 让浏览器下载，并兼容中文文件名
      if (downloadFilename != null && downloadFilename.length() > 0) {
        String safe = downloadFilename.replace("\"", "");
        String encoded = URLEncoder.encode(downloadFilename, StandardCharsets.UTF_8).replace("+", "%20");
        String disposition = "attachment; filename=\"" + safe + "\"; filename*=UTF-8''" + encoded;
        req.putCustomRequestHeader("response-content-disposition", disposition);
      } else {
        req.putCustomRequestHeader("response-content-disposition", "attachment");
      }

      if (contentType != null && contentType.length() > 0) {
        req.putCustomRequestHeader("response-content-type", contentType);
      }

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

  // -------------------------
  // Client builder
  // -------------------------

  public static COSClient buildClient() {
    return buildClient();
  }

  public static COSClient buildClient(String regionName) {
    if (regionName == null || regionName.length() == 0) {
      throw new IllegalStateException("TENCENT_COS_REGION_NAME is empty");
    }
    if (bucketName == null || bucketName.length() == 0) {
      throw new IllegalStateException("TENCENT_COS_BUCKET_NAME is empty");
    }
    if (secretId == null || secretId.length() == 0 || secretKey == null || secretKey.length() == 0) {
      throw new IllegalStateException("TENCENT_COS_SECRET_ID / TENCENT_COS_SECRET_KEY is empty");
    }

    COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
    Region region = new Region(regionName);

    ClientConfig clientConfig = new ClientConfig(region);
    clientConfig.setHttpProtocol(HttpProtocol.https);

    return new COSClient(cred, clientConfig);
  }

}