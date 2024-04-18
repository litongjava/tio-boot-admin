package com.litongjava.tio.boot.admin.client;



import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.litongjava.tio.utils.environment.EnvironmentUtils;
import com.litongjava.tio.utils.json.FastJson2Utils;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClaudeClient {
  private static final String API_URL = "https://api.anthropic.com/v1/messages";
  private static final String API_KEY = EnvironmentUtils.get("CLAUDE_API_KEY");
  private static final String MODEL = "claude-3-opus-20240229";
  private static final int MAX_TOKEN = 256;
  private static final boolean STREAM = true;

  public static void streamClaudeMessage(OkHttpClient client, List<MessageItem> messages, Callback responseCallback) {

    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("model", MODEL);
    reqMap.put("messages", messages);

    reqMap.put("max_tokens", MAX_TOKEN);
    reqMap.put("stream", STREAM);

    String requestJsonString = FastJson2Utils.toJson(reqMap);

    RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJsonString);

    Request request = new Request.Builder().url(API_URL)//
        .post(body) //
        .header("anthropic-version", "2023-06-01")//
        .header("anthropic-beta", "messages-2023-12-15")//
        .header("content-type", "application/json")//
        .header("x-api-key", API_KEY)//
        .build();

    client.newCall(request).enqueue(responseCallback);
  }

  public static Response complete(OkHttpClient client, List<MessageItem> messages) throws IOException {

    Map<String, Object> reqMap = new HashMap<>();
    reqMap.put("model", MODEL);
    reqMap.put("messages", messages);

    reqMap.put("max_tokens", MAX_TOKEN);

    String requestJsonString = FastJson2Utils.toJson(reqMap);

    RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJsonString);

    Request request = new Request.Builder().url(API_URL)//
        .post(body) //
        .header("anthropic-version", "2023-06-01")//
        .header("anthropic-beta", "messages-2023-12-15")//
        .header("content-type", "application/json")//
        .header("x-api-key", API_KEY)//
        .build();

    return client.newCall(request).execute();

  }
}