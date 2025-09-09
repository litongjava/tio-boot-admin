package com.litongjava.tio.boot.admin.handler;

import java.util.Map;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.services.system.SystemUserService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.CORSUtils;
import com.litongjava.tio.utils.json.Json;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class SystemHandler {

  public HttpResponse changeUserPassword(HttpRequest request) {
    HttpResponse response = TioRequestContext.getResponse();
    CORSUtils.enableCORS(response, new HttpCors());

    Map<String, String> requestMap = Json.getJson().parseToMap(request.getBodyString(), String.class, String.class);
    Long userId = TioRequestContext.getUserIdLong();
    RespBodyVo respVo = Aop.get(SystemUserService.class).changePassword(userId, requestMap);
    response.setJson(respVo);

    return response;

  }

}
