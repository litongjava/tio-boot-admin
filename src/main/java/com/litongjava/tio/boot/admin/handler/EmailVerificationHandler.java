package com.litongjava.tio.boot.admin.handler;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.services.AppEmailService;
import com.litongjava.tio.boot.admin.vo.EmailRequest;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.util.CORSUtils;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.json.JsonUtils;

public class EmailVerificationHandler {
  // 发送验证码邮件
  public HttpResponse sendVerification(HttpRequest request) {
    HttpResponse response = TioRequestContext.getResponse();
    String origin = request.getOrigin();
    CORSUtils.enableCORS(response);
    String body = request.getBodyString();
    EmailRequest req = JsonUtils.parse(body, EmailRequest.class);

    AppEmailService emailService = Aop.get(AppEmailService.class);
    boolean sent = emailService.sendVerificationEmail(req.getEmail(), origin);
    if (sent) {
      return response.setJson(RespBodyVo.ok());
    }
    return Resps.json(response, RespBodyVo.fail());
  }

  // 验证邮箱验证码
  public HttpResponse verifyEmail(HttpRequest request) {
    HttpResponse response = TioRequestContext.getResponse();
    CORSUtils.enableCORS(response);
    String email = request.getParameter("email");
    String code = request.getParameter("code");

    AppEmailService emailService = Aop.get(AppEmailService.class);
    boolean verified = emailService.verifyEmailCode(email, code);
    if (verified) {
      return Resps.json(response, RespBodyVo.ok());
    }
    return Resps.json(response, RespBodyVo.fail());
  }
}