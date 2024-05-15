package com.litongjava.tio.boot.admin.client;

import java.io.IOException;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.json.Json;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatGptClient {

  public Response completions(CompletionsModel model) {
    String apiKey = EnvUtils.get("OPENAI_API_KEY");

    OkHttpClient client = Aop.get(OkHttpClient.class);

    String url = "https://api.openai.com/v1/chat/completions";
    MediaType mediaType = MediaType.parse("application/json");
    String content = Json.getJson().toJson(model);

    RequestBody body = RequestBody.create(mediaType, content);

    Request request = new Request.Builder() //
        .url(url) //
        .method("POST", body) //
        .addHeader("Content-Type", "application/json") //
        .addHeader("Authorization", "Bearer " + apiKey) //
        .build();
    try {
      return client.newCall(request).execute();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}