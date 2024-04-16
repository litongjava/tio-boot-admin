package com.litongjava.tio.boot.admin.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.services.SystemUserService;
import com.litongjava.tio.boot.http.TioControllerContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.HttpServerResponseUtils;
import com.litongjava.tio.utils.json.Json;
import com.litongjava.tio.utils.resp.RespVo;

import java.util.Map;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class SystemHandler {

  public HttpResponse changeUserPassword(HttpRequest request) {
    HttpResponse response = TioControllerContext.getResponse();
    HttpServerResponseUtils.enableCORS(response, new HttpCors());

    Map<String, String> requestMap = Json.getJson().parseToMap(request.getBodyString(), String.class, String.class);
    Object userId = StpUtil.getLoginId();
    RespVo respVo = Aop.get(SystemUserService.class).changePassword(Long.parseLong((String) userId), requestMap);
    response.setJson(respVo);

    return response;

  }


}
