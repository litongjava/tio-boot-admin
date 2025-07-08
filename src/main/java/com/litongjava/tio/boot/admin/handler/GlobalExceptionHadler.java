package com.litongjava.tio.boot.admin.handler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;

import com.litongjava.constants.ServerConfigKeys;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.agent.NotificationSender;
import com.litongjava.tio.boot.exception.TioBootExceptionHandler;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.core.ChannelContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.utils.HttpIpUtils;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.network.IpUtils;
import com.litongjava.tio.utils.notification.NotifactionWarmModel;
import com.litongjava.tio.websocket.common.WebSocketRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalExceptionHadler implements TioBootExceptionHandler {

  @Override
  public RespBodyVo handler(HttpRequest request, Throwable e) {
    String requestId = request.getChannelContext().getId();

    String requestLine = request.getRequestLine().toString();
    String host = request.getHost();
    Map<String, String> headers = request.getHeaders();
    String bodyString = request.getBodyString();

    String realIp = HttpIpUtils.getRealIp(request);

    // 获取完整的堆栈跟踪
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    String stackTrace = sw.toString();

    log.info("requestId,{},{},{},{},{}", requestId, host, requestLine, headers, bodyString, stackTrace);
    NotifactionWarmModel model = new NotifactionWarmModel();

    String localIp = IpUtils.getLocalIp();
    model.setAppEnv(EnvUtils.env());
    model.setAppGroupName("tio-boot");
    model.setAppName(EnvUtils.get(ServerConfigKeys.APP_NAME));
    model.setWarningName("GlobalExceptionHadler");
    model.setLevel("LeveL 1");

    model.setDeviceName(localIp);
    model.setTime(ZonedDateTime.now());
    model.setRequestId(requestId);
    model.setUserIp(realIp);
    model.setUserId(TioRequestContext.getUserIdString());
    model.setHost(host);
    model.setRequestLine(requestLine);
    model.setRequestBody(bodyString);
    model.setStackTrace(stackTrace);
    model.setContent("unknow error");

    if (!EnvUtils.isDev()) {
      NotificationSender notificationSender = TioBootServer.me().getNotificationSender();
      if (notificationSender != null) {
        notificationSender.send(model);
      }
    }

    return RespBodyVo.fail(e.getMessage());
  }

  @Override
  public Object wsTextHandler(WebSocketRequest webSokcetRequest, String text, ChannelContext channelContext, HttpRequest httpRequest, Throwable e) {
    return null;
  }

  @Override
  public Object wsBytesHandler(WebSocketRequest webSokcetRequest, byte[] bytes, ChannelContext channelContext, HttpRequest httpRequest, Throwable e) {
    return null;
  }
}