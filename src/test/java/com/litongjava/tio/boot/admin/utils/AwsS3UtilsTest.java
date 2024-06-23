package com.litongjava.tio.boot.admin.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.litongjava.tio.boot.admin.config.AwsS3Config;
import com.litongjava.tio.utils.environment.EnvUtils;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class AwsS3UtilsTest {

  @Test
  public void test() throws IOException {
    EnvUtils.load();

    Path path = java.nio.file.Paths.get("F:\\my_file\\my_photo\\kitty\\kitty-cat.jpg");

    byte[] fileContent = Files.readAllBytes(path);

    String targetName = "001.jpg"; // 上传到S3的文件名
    String suffix = "jpg";

    // 示例使用upload方法
    try (S3Client client = new AwsS3Config().buildClient();) {
      PutObjectResponse response = AwsS3Utils.upload(client, AwsS3Utils.bucketName, targetName, fileContent, suffix);
      String url = String.format(AwsS3Utils.urlFormat, AwsS3Utils.bucketName, targetName);
      System.out.println(url);
      System.out.println(response.eTag());
    }
  }

}
