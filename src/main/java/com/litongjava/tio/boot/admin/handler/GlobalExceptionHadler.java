package com.litongjava.tio.boot.admin.handler;

import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.exception.TioBootExceptionHandler;
import com.litongjava.tio.boot.sender.NotifactionWarmUtils;
import com.litongjava.tio.boot.sender.NotificationSender;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.core.ChannelContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.notification.NotifactionWarmModel;
import com.litongjava.tio.websocket.common.WebSocketRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GlobalExceptionHadler implements TioBootExceptionHandler {

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