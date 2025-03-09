package com.litongjava.tio.boot.admin.handler;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.services.AppUserService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;

public class AppUserAnonymousHandler {
  public HttpResponse create(HttpRequest request) {
    String origin = request.getOrigin();
    HttpResponse response = TioRequestContext.getResponse();

    AppUserService appUserService = Aop.get(AppUserService.class);
    // 注册用户（内部会处理密码加盐和哈希等逻辑）
    RespBodyVo vo = appUserService.createAnonymousUser(origin);
    return response.setJson(vo);

  }
}