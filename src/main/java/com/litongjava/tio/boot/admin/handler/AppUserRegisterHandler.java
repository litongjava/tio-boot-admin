package com.litongjava.tio.boot.admin.handler;

import java.util.ArrayList;
import java.util.List;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.validate.ValidateResult;
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
    AppUserRegisterRequest req = Json.getJson().parse(body, AppUserRegisterRequest.class);

    List<ValidateResult> validateResults = new ArrayList<>();
    boolean ok = true;
    if (req.getVerification_type() != 0) {
      if (StrUtil.isEmpty(origin)) {
        ValidateResult validateResult = ValidateResult.by("origin", "Failed to valiate origin:" + origin);
        validateResults.add(validateResult);
        ok = false;
      }
    }

    // 解析注册请求参数
    boolean validate = false;
    String username = req.getUsername();
    String email = req.getEmail();
    if (username == null && email == null) {
      ValidateResult validateResult = ValidateResult.by("username", "Username and email cannot both be empty.");
      validateResults.add(validateResult);
      ok = false;
    }
    if (email != null) {
      validate = EmailValidator.validate(email);
      if (!validate) {
        ValidateResult validateResult = ValidateResult.by("email", "Failed to valiate email:" + email);
        validateResults.add(validateResult);
        ok = false;
      }
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

    if (email != null) {
      boolean exists = appUserService.existsEmail(email);
      if (exists) {
        ValidateResult validateResult = ValidateResult.by("email", "email already taken" + email);
        validateResults.add(validateResult);
        ok = false;
      }
    }

    if (username != null) {
      boolean exists = appUserService.existsUsername(username);
      if (exists) {
        ValidateResult validateResult = ValidateResult.by("email", "username already taken" + email);
        validateResults.add(validateResult);
        ok = false;
      }

    }

    if (!ok) {
      return response.setJson(RespBodyVo.failData(validateResults));
    }

    boolean success = false;
    Long userId = req.getUserId();
    if (userId != null && appUserService.exists(userId.toString())) {
      success = appUserService.registerUserByUserId(req, origin);
    } else {
      // 注册用户（内部会处理密码加盐和哈希等逻辑）
      success = appUserService.registerUser(req.getEmail(), req.getUsername(), req.getPassword(), req.getUserType(),
          origin);
    }

    if (success) {
      if (req.getVerification_type() == 1) {
        // 注册成功后发送验证邮件（验证码及链接）
        AppEmailService emailService = Aop.get(AppEmailService.class);
        boolean sent = emailService.sendVerificationCodeEmail(req.getEmail(), origin);
        if (sent) {
          return response.setJson(RespBodyVo.ok());
        } else {
          return response.setJson(RespBodyVo.fail("Failed to send email"));
        }
      } else if (req.getVerification_type() == 2) {
        // 注册成功后发送验证邮件（验证码及链接）
        AppEmailService emailService = Aop.get(AppEmailService.class);
        boolean sent = emailService.sendVerificationEmail(req.getEmail(), origin);
        if (sent) {
          return response.setJson(RespBodyVo.ok());
        } else {
          return response.setJson(RespBodyVo.fail("Failed to send email"));
        }
      } else {
        return response.setJson(RespBodyVo.ok());
      }
    } else {
      return response.setJson(RespBodyVo.fail());
    }
  }
}