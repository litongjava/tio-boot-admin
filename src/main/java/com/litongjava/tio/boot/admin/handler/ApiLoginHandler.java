package com.litongjava.tio.boot.admin.handler;

import com.jfinal.kit.Kv;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.services.LoginService;
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

    RespVo respVo;
    Long userId = Aop.get(LoginService.class).getUserIdByUsernameAndPassword(loginAccountVo);
    if (userId != null) {
      //登录
      StpUtil.login(userId);
      String tokenValue = StpUtil.getTokenValue();
      long tokenTimeout = StpUtil.getTokenTimeout();
      Kv kv = new Kv();
      kv.set("token", tokenValue);
      kv.set("tokenTimeout", tokenTimeout);
      kv.set("type", loginAccountVo.getType());
      kv.set("status", "ok");

      respVo = RespVo.ok(kv);
    } else {
      respVo = RespVo.fail();
    }

    HttpResponse response = TioControllerContext.getResponse();
    return Resps.json(response, respVo);
  }
}
