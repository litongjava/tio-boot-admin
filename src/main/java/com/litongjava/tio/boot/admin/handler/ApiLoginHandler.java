package com.litongjava.tio.boot.admin.handler;

import com.litongjava.tio.boot.admin.vo.LoginAccountVo;
import com.litongjava.tio.boot.http.TioControllerContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.json.Json;
import com.litongjava.tio.utils.resp.RespVo;

import cn.dev33.satoken.stp.StpUtil;

public class ApiLoginHandler {
  public HttpResponse account(HttpRequest request) {
    String bodyString = request.getBodyString();
    LoginAccountVo loginAccountVo = Json.getJson().parse(bodyString, LoginAccountVo.class);
    
    //登录
    StpUtil.login(loginAccountVo.getUsername());

    RespVo respVo = RespVo.ok(loginAccountVo);
    HttpResponse response = TioControllerContext.getResponse();
    return Resps.json(response, respVo);
  }

  public HttpResponse validateToken(HttpRequest request) {
    RespVo respVo;
    try {
      StpUtil.checkActiveTimeout();
      respVo = RespVo.ok();
    } catch (Exception e) {
      respVo = RespVo.fail(e.getMessage());
    }
    return Resps.json(request, respVo);
  }
}
