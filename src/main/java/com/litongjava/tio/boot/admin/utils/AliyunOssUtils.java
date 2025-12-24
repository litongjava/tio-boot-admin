package com.litongjava.tio.boot.admin.utils;

import java.io.ByteArrayInputStream;
import java.io.File;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.http.ContentTypeUtils;
import com.litongjava.tio.utils.hutool.FilenameUtils;

/**
 * Aliyun OSS 工具类 功能类似 AwsS3Utils，用于统一上传与 URL 获取。
 */
public class AliyunOssUtils {

  /**
   * URL 模板: https://<bucket>.oss-<region>.aliyuncs.com/<objectKey>
   */
  public static final String urlFormat = "https://%s.oss-%s.aliyuncs.com/%s";

  // 从环境变量或配置文件读取配置
  public static final String bucketName = EnvUtils.get("OSS_BUCKET_NAME");
  public static final String regionName = EnvUtils.get("OSS_REGION_NAME");
  public static final String endpoint = EnvUtils.get("OSS_ENDPOINT");
  public static final String accessKeyId = EnvUtils.get("OSS_ACCESS_KEY_ID");
  public static final String accessKeySecret = EnvUtils.get("OSS_ACCESS_KEY_SECRET");

  /**
   * 构建 OSS 客户端
   */
  public static OSS buildClient() {
    try {
      ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
      conf.setSignatureVersion(SignVersion.V4);

      // 使用明文 AccessKey 创建客户端（与 AWS 逻辑对齐）
      DefaultCredentialProvider defaultCredentialProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

      OSS ossClient = OSSClientBuilder.create()
          //
          .endpoint(endpoint)
          //
          .credentialsProvider(defaultCredentialProvider)
          //
          .clientConfiguration(conf).region(regionName).build();
      return ossClient;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to build Aliyun OSS client", e);
    }
  }

  /**
   * 上传文件
   */
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

  /**
   * 拼接文件访问 URL
   */
  public static String getUrl(String bucketName, String objectKey) {
    return String.format(urlFormat, bucketName, regionName, objectKey);
  }

  public static String getUrl(String objectKey) {
    return getUrl(bucketName, objectKey);
  }
}
