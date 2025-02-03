package com.litongjava.tio.boot.admin.services;

import com.litongjava.tio.boot.admin.vo.AccessLogVo;
import com.litongjava.tio.http.common.HttpMethod;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.RequestLine;
import com.litongjava.tio.http.common.utils.HttpIpUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccessLogService {

  public AccessLogVo parseToAccessLog(HttpRequest request) {
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

    AccessLogVo accessLogVo = new AccessLogVo();
    accessLogVo.setId(SnowflakeIdUtils.id()).setChannel_id(channel_id).setClientIp(clientIp).setUserId(userId)
        //
        .setMethod(method.toString()).setUri(uri).setUser_agent(userAgent).setHeader(header.toString()).setBody(bodyString);
    return accessLogVo;
  }
}