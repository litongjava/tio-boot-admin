package com.litongjava.tio.boot.admin.handler;

import com.alibaba.fastjson2.JSONObject;
import com.litongjava.tio.boot.admin.services.UserEventService;
import com.litongjava.tio.utils.json.FastJson2Utils;

import nexus.io.jfinal.aop.Aop;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;
import nexus.io.tio.http.server.model.HttpCors;
import nexus.io.tio.http.server.util.CORSUtils;

/**
 * Created by litonglinux@qq.com on 3/25/2024_7:22 PM
 */
public class UserEventHandler {

  public HttpResponse add(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

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
