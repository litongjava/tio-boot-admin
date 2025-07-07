package com.litongjava.tio.boot.admin.handler;

import java.util.ArrayList;
import java.util.List;

import com.litongjava.db.activerecord.Db;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.validate.ValidateResult;
import com.litongjava.tio.boot.admin.costants.TioBootAdminTableNames;
import com.litongjava.tio.boot.admin.services.AppEmailService;
import com.litongjava.tio.boot.admin.services.AppUserService;
import com.litongjava.tio.boot.admin.vo.AppUserRegisterRequest;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.utils.hutool.StrUtil;
import com.litongjava.tio.utils.json.Json;
import com.litongjava.tio.utils.validator.EmailValidator;
import com.litongjava.tio.utils.validator.PasswordValidator;

public class AppUserRegisterHandler {
  AppUserService appUserService = Aop.get(AppUserService.class);
  
  public HttpResponse register(HttpRequest request) {
    String origin = request.getOrigin();
    HttpResponse response = TioRequestContext.getResponse();
    String body = request.getBodyString();

    List<ValidateResult> validateResults = new ArrayList<>();
    boolean ok = true;
    if (StrUtil.isEmpty(origin)) {
      ValidateResult validateResult = ValidateResult.by("origin", "Failed to valiate origin:" + origin);
      validateResults.add(validateResult);
      ok = false;
    }

    // 解析注册请求参数
    AppUserRegisterRequest req = Json.getJson().parse(body, AppUserRegisterRequest.class);
    String email = req.getEmail();
    boolean validate = EmailValidator.validate(email);
    if (!validate) {
      ValidateResult validateResult = ValidateResult.by("eamil", "Failed to valiate email:" + email);
      validateResults.add(validateResult);
      ok = false;
    }

    String password = req.getPassword();
    validate = PasswordValidator.validate(password);
    if (!validate) {
      ValidateResult validateResult = ValidateResult.by("password", "Failed to valiate password:" + password);
      validateResults.add(validateResult);
      ok = false;
    }

    if (!ok) {
      return response.setJson(RespBodyVo.failData(validateResults));
    }

    boolean exists =appUserService.existsEmail(email); 
    if (exists) {
      ValidateResult validateResult = ValidateResult.by("eamil", "Eamil already taken" + email);
      validateResults.add(validateResult);
    }

    if (!ok) {
      return response.setJson(RespBodyVo.failData(validateResults));
    }

    boolean success = false;
    Long userId = req.getUserId();
    if (userId != null && appUserService.exists(userId.toString())) {
      success = appUserService.registerUserByUserId(req,origin);
    } else {
      // 注册用户（内部会处理密码加盐和哈希等逻辑）
      success = appUserService.registerUser(req.getEmail(), req.getPassword(), req.getUserType(), origin);
    }

    if (success) {
      // 注册成功后发送验证邮件（验证码及链接）
      AppEmailService emailService = Aop.get(AppEmailService.class);
      boolean sent = emailService.sendVerificationEmail(req.getEmail(), origin);
      if (sent) {
        return response.setJson(RespBodyVo.ok());
      } else {
        return response.setJson(RespBodyVo.fail("Failed to send email"));
      }
    } else {
      return response.setJson(RespBodyVo.fail());
    }
  }
}