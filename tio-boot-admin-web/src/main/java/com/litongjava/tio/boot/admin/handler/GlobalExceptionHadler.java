package com.litongjava.tio.boot.admin.handler;

import lombok.extern.slf4j.Slf4j;
import nexus.io.model.body.RespBodyVo;
import nexus.io.tio.boot.exception.TioBootExceptionHandler;
import nexus.io.tio.boot.sender.NotifactionWarmUtils;
import nexus.io.tio.core.ChannelContext;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.utils.context.TioAppCan;
import nexus.io.tio.utils.environment.EnvUtils;
import nexus.io.tio.utils.notification.NotifactionWarmModel;
import nexus.io.tio.utils.notification.NotificationSender;
import nexus.io.tio.websocket.common.WebSocketRequest;

@Slf4j
public class GlobalExceptionHadler implements TioBootExceptionHandler {
  private boolean sendIfDev = false;

  public GlobalExceptionHadler() {
    
  }
  public GlobalExceptionHadler(boolean sendIfDev) {
    this.sendIfDev = sendIfDev;
  }

  @Override
  public RespBodyVo handler(HttpRequest request, Throwable e) {

    String appGroupName = "tio-boot";
    String warningName = "GlobalExceptionHadler";
    String level = "LeveL 1";

    NotifactionWarmModel model = NotifactionWarmUtils.toWarmModel(appGroupName, warningName, level, request, e);
    String requestId = model.getRequestId();
    String host = model.getHost();
    String requestLine = model.getRequestLine();
    String bodyString = model.getRequestBody();
    String stackTrace = model.getStackTrace();
    log.info("requestId,{},{},{},{}", requestId, host, requestLine, bodyString, stackTrace);
    model.setContent("unknow error");

    if (EnvUtils.isDev()) {
      if (sendIfDev) {
        NotificationSender notificationSender = TioAppCan.me().getNotificationSender();
        if (notificationSender != null) {
          notificationSender.send(model);
        }
      }
    } else {
      NotificationSender notificationSender = TioAppCan.me().getNotificationSender();
      if (notificationSender != null) {
        notificationSender.send(model);
      }
    }

    return RespBodyVo.fail(e.getMessage());
  }

  @Override
  public Object wsTextHandler(WebSocketRequest webSokcetRequest, String text, ChannelContext channelContext,
      HttpRequest httpRequest, Throwable e) {
    return null;
  }

  @Override
  public Object wsBytesHandler(WebSocketRequest webSokcetRequest, byte[] bytes, ChannelContext channelContext,
      HttpRequest httpRequest, Throwable e) {
    return null;
  }
}