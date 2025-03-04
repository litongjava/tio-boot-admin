package com.litongjava.tio.boot.admin.handler;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.services.DbRequestResponseStatisticsService;
import com.litongjava.tio.boot.http.handler.internal.ResponseStatisticsHandler;
import com.litongjava.tio.http.common.HeaderValue;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.common.RequestLine;
import com.litongjava.tio.http.common.utils.HttpIpUtils;
import com.litongjava.tio.utils.thread.TioThreadUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbRequestResponseStatisticsHandler implements ResponseStatisticsHandler {

  DbRequestResponseStatisticsService service = Aop.get(DbRequestResponseStatisticsService.class);

  @Override
  public void count(HttpRequest request, RequestLine requestLine, HttpResponse httpResponse, Object userId, long elapsed) {

    Long requestId = request.getId();
    String clientIp = HttpIpUtils.getRealIp(request);
    String requestUri = requestLine.toString();
    long id = Long.parseLong(request.getChannelContext().getId());
    String contentType = request.getContentType();

    String authorization = request.getHeader("authorization");
    String token = request.getHeader("token");
    StringBuffer header = new StringBuffer();
    if (authorization != null) {
      header.append("authorization:").append(authorization).append("\n");
    }

    if (token != null) {
      header.append("token:").append(token).append("\n");
    }

    String requestBody = request.getBodyString();

    int responseStatusCode = httpResponse.getStatus().getStatus();
    HeaderValue contentTypeHeader = httpResponse.getContentType();

    // 异步保存
    TioThreadUtils.execute(() -> {
      String responseContentType = null;
      String responseBody = null;
      if (contentTypeHeader != null) {
        responseContentType = contentTypeHeader.toString();
        if (responseContentType.contains("application/json")) {
          byte[] body = httpResponse.getBody();
          if (body != null && body.length > 0 && body.length < 1024) {
            responseBody = new String(body);
            if (responseBody != null && responseBody.indexOf('\0') != -1) {
              responseBody = null;
              log.error("The response_body contains illegal characters and will not save the data.:{}", requestUri);
            }
          }
          service.save(id, requestId, clientIp, requestUri, header.toString(), contentType, requestBody, responseStatusCode, responseBody, userId, elapsed);
        }
      }
    });
    ;
  }
}