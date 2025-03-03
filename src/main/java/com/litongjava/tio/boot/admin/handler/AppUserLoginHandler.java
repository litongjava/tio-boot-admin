package com.litongjava.tio.boot.admin.handler;

import com.jfinal.kit.Kv;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.services.AppUserService;
import com.litongjava.tio.boot.admin.vo.AppUser;
import com.litongjava.tio.boot.admin.vo.AppUserLoginRequest;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.json.JsonUtils;

public class AppUserLoginHandler {
  public HttpResponse login(HttpRequest request) {
    HttpResponse response = TioRequestContext.getResponse();
    String body = request.getBodyString();
    AppUserLoginRequest req = JsonUtils.parse(body, AppUserLoginRequest.class);

    AppUserService appUserService = Aop.get(AppUserService.class);
    AppUser user = appUserService.getUserByEmail(req.getEmail());
    // 此处允许未验证邮箱的用户登录
    if (user != null && appUserService.verifyPassword(user.getId(), req.getPassword())) {
      // 生成 token，有效期 7 天（604800秒）
      Long timeout = EnvUtils.getLong("app.token.timeout", 604800L);
      String token = appUserService.createToken(user.getId(), timeout);
      String refreshToken = appUserService.createRefreshToken(user.getId());

      Kv kv = Kv.by("user_id", user.getId()).set("token", token).set("expires_in", timeout.intValue())
          //
          .set("refresh_token", refreshToken);

      return response.setJson(RespBodyVo.ok(kv));
    }
    return response.setJson(RespBodyVo.fail("username or password is not correct"));
  }
}