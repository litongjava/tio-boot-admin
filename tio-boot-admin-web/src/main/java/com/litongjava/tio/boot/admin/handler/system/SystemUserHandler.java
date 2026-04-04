package com.litongjava.tio.boot.admin.handler.system;

import java.util.Map;

import nexus.io.jfinal.aop.Aop;
import nexus.io.model.body.RespBodyVo;
import nexus.io.tio.boot.admin.services.system.SystemUserService;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;
import nexus.io.tio.http.server.model.HttpCors;
import nexus.io.tio.http.server.util.CORSUtils;
import nexus.io.tio.utils.json.Json;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class SystemUserHandler {

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
