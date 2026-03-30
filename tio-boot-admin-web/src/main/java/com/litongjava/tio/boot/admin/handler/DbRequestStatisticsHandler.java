package com.litongjava.tio.boot.admin.handler;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.services.DbRequestStatisticsService;
import com.litongjava.tio.boot.http.handler.internal.RequestStatisticsHandler;
import com.litongjava.tio.http.common.HttpMethod;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.RequestLine;
import com.litongjava.tio.http.common.utils.HttpIpUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;
import com.litongjava.tio.utils.thread.TioThreadUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbRequestStatisticsHandler implements RequestStatisticsHandler {

  DbRequestStatisticsService dbRequestStatisticsService = Aop.get(DbRequestStatisticsService.class);

  @Override
  public void count(HttpRequest request) {
    RequestLine requestLine = request.getRequestLine();
    HttpMethod method = requestLine.getMethod();
    String uri = requestLine.getPathAndQuery();
    long channel_id = Long.parseLong(request.getChannelContext().getId());
    String userAgent = request.getUserAgent();
    String authorization = request.getHeader("authorization");

    String token = request.getHeader("token");

    StringBuffer header = new StringBuffer();
    if (authorization != null) {
      header.append("authorization:").append(authorization).append("\n");
    }

    if (token != null) {
      header.append("token:").append(token).append("\n");
    }

    String bodyString = request.getBodyString();

    // 接口访问统计在拦截器之前运行,此时 还有解出id
    Object userId = request.getAttribute("userId");
    String clientIp = HttpIpUtils.getRealIp(request);

    // 使用ExecutorService异步执行任务
    TioThreadUtils.submit(() -> {
      try {
        long id = SnowflakeIdUtils.id();
        dbRequestStatisticsService.saveDb(id, channel_id, clientIp, userId, method.toString(), uri, userAgent, header.toString(), bodyString);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    });
  }
}