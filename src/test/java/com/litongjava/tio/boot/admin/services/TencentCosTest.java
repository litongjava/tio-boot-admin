package com.litongjava.tio.boot.admin.services;

import cn.hutool.core.io.file.FileNameUtil;
import com.litongjava.tio.boot.admin.vo.SystemTxCosConfigVo;
import com.litongjava.tio.utils.http.ContentTypeUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class TencentCosTest {

  @Test
  public void testUpload() {
    // Initialize the COS configuration object
    SystemTxCosConfigVo cosConfig = new SystemTxCosConfigVo();
    cosConfig.setSecretId("YOUR_SECRET_ID");
    cosConfig.setSecretKey("YOUR_SECRET_KEY");
    cosConfig.setRegion("YOUR_REGION");
    cosConfig.setBucketName("YOUR_BUCKET_NAME");

    // Prepare a byte array to upload
    byte[] dataToUpload = "Hello, Tencent COS!".getBytes();

    ByteArrayInputStream inputStream = new ByteArrayInputStream(dataToUpload);

    // Generate a key for the object storage (can customize based on needs)
    TencentCOSService cosService = new TencentCOSService();
    String key = "example/file.txt";
    String suffix = FileNameUtil.getSuffix(key);
    String contentType = ContentTypeUtils.getContentType(suffix);
    // Perform the upload
    String result = cosService.simpleUploadFileFromStream(
      cosConfig,
      key,
      contentType, // Content-Type
      inputStream,
      dataToUpload.length // Size of the data to upload
    );
    if (result != null) {
      System.out.println("Upload successful. ETag: " + result);
    } else {
      System.out.println("Upload failed.");
    }
  }
}
