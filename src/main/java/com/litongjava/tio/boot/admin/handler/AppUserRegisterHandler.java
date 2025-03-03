package com.litongjava.tio.boot.admin.handler;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.services.AppUserService;
import com.litongjava.tio.boot.admin.services.AppEmailService;
import com.litongjava.tio.boot.admin.vo.AppUserRegisterRequest;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.utils.json.Json;

public class AppUserRegisterHandler {
  public HttpResponse register(HttpRequest request) {
    String origin = request.getOrigin();
    HttpResponse response = TioRequestContext.getResponse();
    String body = request.getBodyString();

    // 解析注册请求参数
    AppUserRegisterRequest req = Json.getJson().parse(body, AppUserRegisterRequest.class);

    AppUserService appUserService = Aop.get(AppUserService.class);
    // 注册用户（内部会处理密码加盐和哈希等逻辑）
    boolean success = appUserService.registerUser(req.getEmail(), req.getPassword(), req.getUserType(), origin);

    if (success) {
      // 注册成功后发送验证邮件（验证码及链接）
      AppEmailService emailService = Aop.get(AppEmailService.class);
      emailService.sendVerificationEmail(req.getEmail(), origin);
      return response.setJson(RespBodyVo.ok());
    } else {
      return response.setJson(RespBodyVo.fail());
    }
  }
}