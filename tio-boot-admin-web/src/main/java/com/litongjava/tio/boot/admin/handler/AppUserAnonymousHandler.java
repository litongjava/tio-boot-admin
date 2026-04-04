package com.litongjava.tio.boot.admin.handler;

import com.litongjava.tio.boot.admin.services.AppUserService;

import nexus.io.jfinal.aop.Aop;
import nexus.io.model.body.RespBodyVo;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;

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