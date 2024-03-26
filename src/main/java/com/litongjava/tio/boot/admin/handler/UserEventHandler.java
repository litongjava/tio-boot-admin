package com.litongjava.tio.boot.admin.handler;

import com.alibaba.fastjson2.JSONObject;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.services.UserEventService;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.utils.json.FastJson2Utils;
import com.litongjava.tio.utils.json.Json;

/**
 * Created by litonglinux@qq.com on 3/25/2024_7:22 PM
 */
public class UserEventHandler {

  public HttpResponse add(HttpRequest request){
    String bodyString = request.getBodyString();
    JSONObject jsonObject = FastJson2Utils.parseObject(bodyString);
    String eventName = jsonObject.getString("name");
    UserEventService userEventService = Aop.get(UserEventService.class);

    JSONObject eventValue = jsonObject.getJSONObject("value");
    userEventService.save(eventName,eventValue);

    return new HttpResponse(request);
  }
}
