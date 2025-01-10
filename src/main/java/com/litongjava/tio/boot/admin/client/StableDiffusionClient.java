package com.litongjava.tio.boot.admin.client;

import java.io.IOException;
import java.util.Map;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.http.common.UploadFile;
import com.litongjava.tio.utils.environment.EnvUtils;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class StableDiffusionClient {

  public static final String generateSd3ServerUrl = "https://api.stability.ai/v2beta/stable-image/generate/sd3";

  public Response generateSd3(UploadFile uploadFile, Map<String, Object> requestMap) {
    String apiKey = EnvUtils.get("SD_API_KEY");

    OkHttpClient client = Aop.get(OkHttpClient.class);

    MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

    for (Map.Entry<String, Object> e : requestMap.entrySet()) {
      builder.addFormDataPart(e.getKey(), (String) e.getValue());
    }
    String strMode = (String) requestMap.get("mode");
    if ("image-to-image".equals(strMode)) {
      RequestBody fileBody = RequestBody.create(uploadFile.getData(),MediaType.parse("image/png"));
      String filename = uploadFile.getName();
      builder.addFormDataPart("image", filename, fileBody);
    }

    MultipartBody body = builder.build();

    Request request = new Request.Builder().url(generateSd3ServerUrl).method("POST", body)
        .addHeader("authorization", "Bearer " + apiKey).addHeader("accept", "image/*")
        .addHeader("Content-Type", "multipart/form-data").build();

    try {
      return client.newCall(request).execute();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
