package com.litongjava.tio.boot.admin.services.ai;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.litongjava.tio.boot.admin.dao.TioLlmGenerateFailedDao;
import com.litongjava.tio.boot.admin.dao.TioLlmUsageDao;
import com.litongjava.tio.boot.admin.utils.TioAdminEnvUtils;

import nexus.io.api.ApiCooldownManager;
import nexus.io.chat.UniChatClient;
import nexus.io.chat.UniChatRequest;
import nexus.io.chat.UniChatResponse;
import nexus.io.consts.ModelPlatformName;
import nexus.io.exception.GenerateException;
import nexus.io.jfinal.aop.Aop;
import nexus.io.tio.utils.SystemTimer;
import nexus.io.tio.utils.context.TioAppCan;
import nexus.io.tio.utils.environment.EnvUtils;
import nexus.io.tio.utils.json.FastJson2Utils;
import nexus.io.tio.utils.notification.NotifactionWarmModel;
import nexus.io.tio.utils.notification.NotificationSender;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

public class UniPredictService {

  private static final Logger log = LoggerFactory.getLogger(UniPredictService.class);
  private TioLlmGenerateFailedDao tioLlmGenerateFailedDao = Aop.get(TioLlmGenerateFailedDao.class);
  private TioLlmUsageDao tioLlmUsageDao = Aop.get(TioLlmUsageDao.class);

  // 与 PredictService 对齐：可配置化更好，这里先写死，便于迁移
  private static final int MAX_ATTEMPTS = 10; // 你原来是 10 次，这里保持一致
  private static final long DEFAULT_RETRY_DELAY_SECONDS = 30;
  private static final long DEFAULT_RETRY_DELAY_MILLIS = TimeUnit.SECONDS.toMillis(DEFAULT_RETRY_DELAY_SECONDS);

  private static final ApiCooldownManager apiCooldownManager = new ApiCooldownManager();

  public UniChatResponse generate(UniChatRequest uniChatRequest) {
    // 0) 环境策略前置
    applyChinaProxyIfNeeded(uniChatRequest);

    Exception lastException = null;

    // 1) 选择全局冷却 key：沿用 PredictService 的思路（apiKey 维度）
    // 若 apiKey 为空，可降级为 platform 维度，避免 NPE
    String serviceKey = uniChatRequest.getApiKey();
    String platform = uniChatRequest.getPlatform();
    if (serviceKey == null || serviceKey.isEmpty()) {
      serviceKey = platform;
    }

    Long taskId = uniChatRequest.getTaskId();
    String taskName = uniChatRequest.getTaskName();
    for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
      String model = uniChatRequest.getModel();
      try {
        // 2) 全局冷却：避免并发场景下的 429 风暴
        if (isNeedCooldown(platform)) {
          apiCooldownManager.enforceCooldown(serviceKey);
        }

        long start = SystemTimer.currTime;
        UniChatResponse uniChatResponse = UniChatClient.generate(uniChatRequest);
        long end = SystemTimer.currTime;
        long elapsed = end - start;

        if (uniChatResponse == null) {
          log.warn("uniChatResponse is null, taskId={}, attempt={}/{}", taskId, attempt, MAX_ATTEMPTS);
          // null 当作可重试异常处理
          if (attempt < MAX_ATTEMPTS) {
            sleepSafely(DEFAULT_RETRY_DELAY_MILLIS, uniChatRequest, attempt, serviceKey, "null response");
            continue;
          }
          return null;
        }

        // 3) 成功落库：保留你的落库逻辑
        tioLlmUsageDao.saveUsage(uniChatRequest, uniChatResponse, elapsed);

        log.info("LLM generate success. taskId={}, taskName={}, platform={}, model={}, attempt={}, elapsedMs={}",
            //
            taskId, taskName, platform, model, attempt, elapsed);

        return uniChatResponse;

      } catch (GenerateException e) {
        lastException = e;

        // 4) 失败信息采集
        String urlPrefix = e.getUrlPerfix();
        String requestJson = e.getRequestBody();
        Integer statusCode = e.getStatusCode();
        String responseBody = e.getResponseBody();

        String stackTrace = toStackTrace(e);

        // 5) 失败落库
        try {
          tioLlmGenerateFailedDao.save(uniChatRequest, e, stackTrace);
        } catch (Exception saveEx) {
          log.error("Failed to save generate failed record. taskId={}", taskId, saveEx);
        }

        // 6) 分级告警
        sendAlertByStatusCode(uniChatRequest, e, urlPrefix, requestJson, statusCode, responseBody, stackTrace);

        // 7) 判定是否继续重试
        if (!shouldRetry(uniChatRequest, e, statusCode, responseBody)) {
          break;
        }

        // 8) 计算本次退避时间
        long retryDelayMillis = computeRetryDelayMillis(uniChatRequest, e, responseBody);

        // 9) 如果是 Gemini 429：更新全局冷却
        if (ModelPlatformName.GOOGLE.equals(platform) && statusCode != null && statusCode == 429) {
          if (isNeedCooldown(platform)) {
            apiCooldownManager.recordCooldown(serviceKey, retryDelayMillis);
          }
        }

        // 10) 本请求线程也睡眠退避
        if (attempt < MAX_ATTEMPTS) {
          sleepSafely(retryDelayMillis, uniChatRequest, attempt, serviceKey, "GenerateException " + statusCode);
        }

      } catch (InterruptedException ie) {
        // 来自 enforceCooldown 或 sleepSafely
        Thread.currentThread().interrupt();
        lastException = ie;
        throw new RuntimeException("Interrupted during cooldown/backoff. taskId=" + taskId, ie);

      } catch (Exception e) {
        lastException = e;

        // 其他异常（网络、序列化等），可告警 + 默认退避重试
        String warningName = "UniPredictService Generic Exception";

        NotificationSender notificationSender = TioAppCan.me().getNotificationSender();
        if (notificationSender != null) {
          NotifactionWarmModel warmModel = NotifactionWarmModel.fromException(warningName, "I", e.getMessage(), e);
          notificationSender.send(warmModel);
        }

        log.error("Generic exception. taskId={}, platform={}, model={}, attempt={}/{}", taskId, platform, model,
            attempt, MAX_ATTEMPTS, e);

        if (attempt < MAX_ATTEMPTS) {
          sleepSafely(DEFAULT_RETRY_DELAY_MILLIS, uniChatRequest, attempt, serviceKey, "generic exception");
        }
      }
    }

    // 11) 所有尝试失败：抛出统一异常
    if (lastException instanceof RuntimeException) {
      throw (RuntimeException) lastException;
    }
    if (lastException != null) {
      throw new RuntimeException("Failed to generate after retries. taskId=" + taskId, lastException);
    }
    return null;
  }

  private boolean isNeedCooldown(String platform) {
    if (ModelPlatformName.OPENROUTER.equals(platform)) {
      return false;
    } else if (ModelPlatformName.EXCHANGE_TOKEN.equals(platform)
        //
        || ModelPlatformName.EXCHANGE_TOKEN_GOOGLE.equals(platform)
        //
        || ModelPlatformName.EXCHANGE_TOKEN_US.equals(platform)) {
      return false;
    }
    return true;
  }

  private void applyChinaProxyIfNeeded(UniChatRequest uniChatRequest) {
    String platform = uniChatRequest.getPlatform();
    boolean china = TioAdminEnvUtils.isChina();
    if (china) {
      if (ModelPlatformName.OPENROUTER.equals(platform)) {
        String basePrefixUrl = EnvUtils.getStr("OPENROUTER_PROXY_BASE_URL");
        if (basePrefixUrl != null && !basePrefixUrl.isEmpty()) {
          uniChatRequest.setApiPrefixUrl(basePrefixUrl);
        }
      } else if (ModelPlatformName.ANTHROPIC.equals(platform)) {
        String basePrefixUrl = EnvUtils.getStr("ANTHROPIC_PROXY_PREFIX_URL");
        if (basePrefixUrl != null && !basePrefixUrl.isEmpty()) {
          uniChatRequest.setApiPrefixUrl(basePrefixUrl);
        }
      }
    }
  }

  private boolean shouldRetry(UniChatRequest req, GenerateException e, Integer code, String responseBody) {
    if (code == null) {
      return true;
    }

    // Gemini 403 直接抛出（一般是权限/配额策略类错误）
    if (ModelPlatformName.GOOGLE.equals(req.getPlatform()) && code == 403) {
      throw e;
    }

    // 400: 特定余额不足错误直接停止重试
    if (code == 400 && responseBody != null && !responseBody.isEmpty()) {
      try {
        JSONObject errorJsonObject = FastJson2Utils.parseObject(responseBody);
        if (errorJsonObject.containsKey("error")) {
          JSONObject errorObject = errorJsonObject.getJSONObject("error");
          if (errorObject != null && errorObject.containsKey("message")) {
            String message = errorObject.getString("message");
            if (message != null && message.startsWith("Your credit balance is too low to access the")) {
              return false;
            }
          }
        }
      } catch (Exception parseEx) {
        // 解析失败不影响重试决策
      }
    }

    // 其他状态码默认可重试（次数受 MAX_ATTEMPTS 限制）
    return true;
  }

  private long computeRetryDelayMillis(UniChatRequest req, GenerateException e, String responseBody) {
    Integer code = e.getStatusCode();

    // Gemini 429：优先解析 RetryInfo.retryDelay
    if (ModelPlatformName.GOOGLE.equals(req.getPlatform()) && code != null && code == 429) {
      return extractRetryDelayMillisFromGeminiError(responseBody, DEFAULT_RETRY_DELAY_MILLIS);
    }

    // 其他情况：默认退避
    return DEFAULT_RETRY_DELAY_MILLIS;
  }

  private void sleepSafely(long millis, UniChatRequest req, int attempt, String serviceKey, String reason) {
    if (millis <= 0) {
      return;
    }
    try {
      log.info("Backoff sleep. taskId={}, serviceKey={}, attempt={}/{}, sleepMs={}, reason={}", req.getTaskId(),
          serviceKey, attempt, MAX_ATTEMPTS, millis, reason);
      Thread.sleep(millis);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted during backoff sleep. taskId=" + req.getTaskId(), ie);
    }
  }

  private void sendAlertByStatusCode(UniChatRequest req, GenerateException e, String urlPrefix, String requestJson,
      Integer statusCode, String responseBody, String stackTrace) {
    String warningName = "UniPredictService LLM GenerateException: " + req.getTaskName();

    // 400 余额不足、502 特殊处理、其他带 requestJson
    if (statusCode != null && statusCode == 400) {
      // 尝试识别余额不足，余额不足直接发告警但不带大请求体
      boolean lowCredit = false;
      try {
        if (responseBody != null) {
          JSONObject errorJsonObject = FastJson2Utils.parseObject(responseBody);
          JSONObject errorObject = errorJsonObject.getJSONObject("error");
          if (errorObject != null) {
            String message = errorObject.getString("message");
            if (message != null && message.startsWith("Your credit balance is too low to access the")) {
              lowCredit = true;
            }
          }
        }
      } catch (Exception ignore) {
      }

      if (lowCredit) {
        sendWarm(warningName, urlPrefix, null, statusCode, responseBody, stackTrace);
        return;
      }

      // 其他 400：带 requestJson（可选截断）
      sendWarm(warningName, urlPrefix, truncate(requestJson, 4096), statusCode, responseBody, stackTrace);
      return;
    }

    if (statusCode != null && statusCode == 502) {
      sendWarm(warningName, urlPrefix, null, statusCode, responseBody, stackTrace);
      return;
    }

    // 默认：带 requestJson
    sendWarm(warningName, urlPrefix, truncate(requestJson, 4096), statusCode, responseBody, stackTrace);
  }

  private static String truncate(String s, int maxLen) {
    if (s == null) {
      return null;
    }
    if (s.length() <= maxLen) {
      return s;
    }
    return s.substring(0, maxLen);
  }

  private static String toStackTrace(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }

  private static long parseRetryDelayToMillis(String retryDelayStr) {
    if (retryDelayStr == null || retryDelayStr.isEmpty()) {
      return -1;
    }
    try {
      // Gemini 的 retryDelay 通常是类似 "3s"
      String numericPart = retryDelayStr.replaceAll("[^0-9]", "");
      if (numericPart.isEmpty()) {
        return -1;
      }
      long seconds = Long.parseLong(numericPart);
      return TimeUnit.SECONDS.toMillis(seconds);
    } catch (NumberFormatException e) {
      log.error("Failed to parse retryDelay: {}", retryDelayStr, e);
      return -1;
    }
  }

  private static long extractRetryDelayMillisFromGeminiError(String jsonErrorBody, long defaultDelayMillis) {
    if (jsonErrorBody == null || jsonErrorBody.isEmpty()) {
      return defaultDelayMillis;
    }
    try {
      JSONObject errorResponse = FastJson2Utils.parseObject(jsonErrorBody);
      JSONObject errorObj = errorResponse.getJSONObject("error");
      if (errorObj != null) {
        JSONArray details = errorObj.getJSONArray("details");
        if (details != null) {
          for (int j = 0; j < details.size(); j++) {
            JSONObject detail = details.getJSONObject(j);
            String type = detail.getString("@type");
            if ("type.googleapis.com/google.rpc.RetryInfo".equals(type)) {
              String retryDelayStr = detail.getString("retryDelay");
              long parsedDelay = parseRetryDelayToMillis(retryDelayStr);
              return parsedDelay > 0 ? parsedDelay : defaultDelayMillis;
            }
          }
        }
      }
    } catch (Exception ex) {
      log.error("Error parsing Gemini error response for retryDelay. body={}", jsonErrorBody, ex);
    }
    return defaultDelayMillis;
  }

  public EventSource stream(UniChatRequest uniChatRequest, EventSourceListener listener) {
    applyChinaProxyIfNeeded(uniChatRequest);
    return UniChatClient.stream(uniChatRequest, listener);
  }

  private void sendWarm(String warningName, String urlPrefix, String request, Integer statusCode, String responseBody,
      String stackTrace) {

    NotificationSender notificationSender = TioAppCan.me().getNotificationSender();
    if (notificationSender != null) {
      NotifactionWarmModel warmModel = NotifactionWarmModel.fromException(warningName, "I", "Failed to requst model",
          stackTrace);
      warmModel.setRequestUrl(urlPrefix).setRequestBody(request).setStatusCode(statusCode)
          .setResponseBody(responseBody);
      notificationSender.send(warmModel);
    }
  }
}
