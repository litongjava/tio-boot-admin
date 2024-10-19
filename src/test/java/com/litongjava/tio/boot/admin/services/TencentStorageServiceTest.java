package com.litongjava.tio.boot.admin.services;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.config.DbConfig;
import com.litongjava.tio.boot.tesing.TioBootTest;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.utils.json.FastJson2Utils;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class TencentStorageServiceTest {

  @BeforeClass
  public static void beforeClass() {
    TioBootTest.runWith(DbConfig.class);
  }

  @Test
  public void upload() {
    // Specify the path to your file
    String filePath = "C:\\Users\\Administrator\\Pictures\\gpt-translate.png";

    // Create a Path object
    Path path = Paths.get(filePath);

    // Initialize the UploadFile object
    UploadFile uploadFile = new UploadFile();
    try {
      // Read all bytes from the file
      byte[] fileData = Files.readAllBytes(path);

      // Set data, name, and size in UploadFile
      uploadFile.setData(fileData);
      uploadFile.setName(path.getFileName().toString());
      uploadFile.setSize(fileData.length);

      // Get an instance of TencentStorageService and upload the file
      RespBodyVo uploadResponse = Aop.get(TencentStorageService.class).upload(uploadFile);

      // Print the result
      System.out.println(FastJson2Utils.toJson(uploadResponse));
    } catch (IOException e) {
      // Handle possible I/O errors
      e.printStackTrace();
      fail("Failed to read file or upload: " + e.getMessage());
    }
  }

}