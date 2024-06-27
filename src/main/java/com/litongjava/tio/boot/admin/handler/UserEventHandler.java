package com.litongjava.tio.boot.admin.handler;

import com.alibaba.fastjson2.JSONObject;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.services.UserEventService;
import com.litongjava.tio.boot.http.TioHttpContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.HttpServerResponseUtils;
import com.litongjava.tio.utils.json.FastJson2Utils;

/**
 * Created by litonglinux@qq.com on 3/25/2024_7:22 PM
 */
public class UserEventHandler {

  public HttpResponse add(HttpRequest request) {
    HttpResponse httpResponse = TioHttpContext.getResponse();
    HttpServerResponseUtils.enableCORS(httpResponse, new HttpCors());

    String method = request.getMethod();
    if ("OPTIONS".equals(method)) {
      return httpResponse;
    }
    String bodyString = request.getBodyString();
    JSONObject jsonObject = FastJson2Utils.parseObject(bodyString);
    String eventName = jsonObject.getString("name");
    UserEventService userEventService = Aop.get(UserEventService.class);

    JSONObject eventValue = null;
    try {
      eventValue = jsonObject.getJSONObject("value");
    } catch (Exception e) {
      eventValue = new JSONObject();
      eventValue.put("value", jsonObject.getString("value"));
    }

    userEventService.save(eventName, eventValue);


    return httpResponse;
  }
}
