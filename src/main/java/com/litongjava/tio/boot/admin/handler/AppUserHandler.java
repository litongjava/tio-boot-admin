package com.litongjava.tio.boot.admin.handler;

import java.util.Map;

import com.jfinal.kit.Kv;
import com.litongjava.db.activerecord.Row;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.costants.AppConstant;
import com.litongjava.tio.boot.admin.services.AppUserService;
import com.litongjava.tio.boot.admin.vo.AppUser;
import com.litongjava.tio.boot.admin.vo.UserResetPasswordRequest;
import com.litongjava.tio.boot.admin.vo.UserUpdatePasswordRequest;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.json.FastJson2Utils;
import com.litongjava.tio.utils.json.JsonUtils;
import com.litongjava.tio.utils.jwt.JwtUtils;

public class AppUserHandler {
  public HttpResponse refresh(HttpRequest request) {
    HttpResponse response = TioRequestContext.getResponse();
    String bodyString = request.getBodyString();
    String refresh_token = FastJson2Utils.parseObject(bodyString).getString("refresh_token");

    String key = EnvUtils.getStr(AppConstant.ADMIN_SECRET_KEY);
    boolean verify = JwtUtils.verify(key, refresh_token);
    if (verify) {
      String userId = JwtUtils.parseUserIdString(refresh_token);
      AppUserService appUserService = Aop.get(AppUserService.class);
      // 生成 token，有效期 7 天（604800秒）
      Long timeout = EnvUtils.getLong("app.token.timeout", 604800L);
      Long tokenTimeout = System.currentTimeMillis() / 1000 + timeout;
      String token = appUserService.createToken(userId, tokenTimeout);
      Kv kv = Kv.by("user_id", userId).set("token", token).set("expires_in", tokenTimeout.intValue());
      response.setJson(RespBodyVo.ok(kv));
    } else {
      response.setJson(RespBodyVo.fail("Failed to validate refresh_token"));
    }
    return response;
  }

  public HttpResponse remove(HttpRequest request) {
    HttpResponse response = TioRequestContext.getResponse();
    String userIdString = TioRequestContext.getUserIdString();
    AppUserService appUserService = Aop.get(AppUserService.class);
    boolean ok = appUserService.remove(userIdString);
    if (ok) {
      response.setJson(RespBodyVo.ok());
    } else {
      response.setJson(RespBodyVo.fail());
    }
    return response;
  }

  public HttpResponse resetPassword(HttpRequest request) {
    HttpResponse response = TioRequestContext.getResponse();
    String bodyString = request.getBodyString();
    UserResetPasswordRequest userResetPassword = JsonUtils.parse(bodyString, UserResetPasswordRequest.class);

    AppUserService appUserService = Aop.get(AppUserService.class);
    RespBodyVo vo = appUserService.resetPassword(userResetPassword);
    return response.setJson(vo);
  }

  public HttpResponse profile(HttpRequest request) {
    String userIdString = TioRequestContext.getUserIdString();
    AppUserService appUserService = Aop.get(AppUserService.class);
    AppUser user = appUserService.getUserById(userIdString);
    HttpResponse response = TioRequestContext.getResponse();
    return response.body(RespBodyVo.ok(user));
  }

  public HttpResponse update(HttpRequest request) {
    String userIdString = TioRequestContext.getUserIdString();
    String bodyString = request.getBodyString();
    Map<String, Object> requestMap = FastJson2Utils.parseToMap(bodyString, String.class, Object.class);
    requestMap.remove("password_salt");
    requestMap.remove("password_hash");
    Row row = Row.fromMap(requestMap);
    AppUserService appUserService = Aop.get(AppUserService.class);
    HttpResponse response = TioRequestContext.getResponse();
    return response.body(RespBodyVo.ok(appUserService.updateById(userIdString, row)));
  }

  public HttpResponse updatePassword(HttpRequest request) {
    String userIdString = TioRequestContext.getUserIdString();
    String bodyString = request.getBodyString();
    UserUpdatePasswordRequest updatePasswordRequest = FastJson2Utils.parse(bodyString, UserUpdatePasswordRequest.class);
    AppUserService appUserService = Aop.get(AppUserService.class);
    HttpResponse response = TioRequestContext.getResponse();

    RespBodyVo respBodyvo = appUserService.updatePassword(userIdString, updatePasswordRequest);
    return response.body(respBodyvo);
  }
}