package com.litongjava.tio.boot.admin.ai.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.client.StableDiffusionClient;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.utils.environment.EnvUtils;

import okhttp3.Response;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class StableDiffusionClientTest {

  @Test
  public void generateSd3() {
    EnvUtils.load();
    Map<String, Object> requestMap = new HashMap<>();
    requestMap.put("mode", "image-to-image");
    requestMap.put("output_format", "jpeg");
    requestMap.put("strength", "1");
    requestMap.put("model", "sd3");
    requestMap.put("prompt", "Lighthouse on a cliff overlooking the ocean");

    UploadFile uploadFile = new UploadFile();
    Path path = Paths.get("F:\\my_file\\my_photo\\kitty\\kitty-cat.jpg");
    try {
      byte[] bytes = Files.readAllBytes(path);
      uploadFile.setData(bytes);
      uploadFile.setName(path.getFileName().toString());
      StableDiffusionClient stableDiffusionClient = Aop.get(StableDiffusionClient.class);
      try (Response response = stableDiffusionClient.generateSd3(uploadFile, requestMap);) {
        if (!response.isSuccessful()) {
          System.out.println(response.body().string());
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}