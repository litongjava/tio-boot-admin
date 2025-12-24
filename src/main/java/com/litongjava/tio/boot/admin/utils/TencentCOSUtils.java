package com.litongjava.tio.boot.admin.utils;

import java.io.ByteArrayInputStream;
import java.io.File;

import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.ContentTypeUtils;
import com.litongjava.tio.utils.hutool.FilenameUtils;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;

/**
 * Tencent COS 工具类，功能类似 AliyunOssUtils，用于统一上传与 URL 获取。
 */
public class TencentCOSUtils {

  /**
   * URL 模板: https://<bucket>.cos.<region>.myqcloud.com/<objectKey>
   */
  public static final String urlFormat = "https://%s.cos.%s.myqcloud.com/%s";

  // 从环境变量或配置文件读取配置
  public static final String bucketName = EnvUtils.get("TENCENT_COS_BUCKET_NAME");
  public static final String regionName = EnvUtils.get("TENCENT_COS_REGION_NAME");
  public static final String secretId = EnvUtils.get("TENCENT_COS_SECRET_ID");
  public static final String secretKey = EnvUtils.get("TENCENT_COS_SECRET_KEY");

  /**
   * 构建 COS 客户端
   */
  public static COSClient buildClient() {
    try {
      COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
      Region region = new Region(regionName);

      ClientConfig clientConfig = new ClientConfig(region);
      clientConfig.setHttpProtocol(HttpProtocol.https);

      return new COSClient(cred, clientConfig);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to build Tencent COS client", e);
    }
  }

  /**
   * 上传文件（字节数组）
   *
   * @param client    COSClient 实例
   * @param bucket    bucket 名称
   * @param objectKey 对象 key
   * @param bytes     文件字节
   * @param suffix    文件后缀（用于推断 Content-Type），如 ".jpg"
   */
  public static PutObjectResult upload(COSClient client, String bucket, String objectKey, byte[] bytes, String suffix) {
    try {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(bytes.length);
      if (suffix != null) {
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
   * 上传文件（File）
   *
   * @param client    COSClient 实例
   * @param bucket    bucket 名称
   * @param objectKey 对象 key
   * @param file      文件
   */
  public static PutObjectResult upload(COSClient client, String bucket, String objectKey, File file) {
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

      PutObjectRequest req = new PutObjectRequest(bucket, objectKey, file);
      req.setMetadata(metadata);

      return client.putObject(req);
    } catch (Exception e) {
      throw new RuntimeException("Tencent COS upload error", e);
    }
  }

  /**
   * 拼接文件访问 URL
   *
   * @param bucket    bucket 名称
   * @param objectKey 对象 key
   * @return 访问 URL
   */
  public static String getUrl(String bucket, String objectKey) {
    return String.format(urlFormat, bucket, regionName, objectKey);
  }

  public static String getUrl(String objectKey) {
    return getUrl(bucketName, objectKey);
  }
}
