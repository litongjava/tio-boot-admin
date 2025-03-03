package com.litongjava.tio.boot.admin.handler;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.kit.Kv;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.model.token.AuthToken;
import com.litongjava.tio.boot.admin.costants.AppConstant;
import com.litongjava.tio.boot.admin.services.LoginService;
import com.litongjava.tio.boot.admin.vo.LoginAccountVo;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.CORSUtils;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.json.Json;
import com.litongjava.tio.utils.jwt.JwtUtils;
import com.litongjava.tio.utils.token.TokenManager;

public class ApiLoginHandler {
  public HttpResponse account(HttpRequest request) {

    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    String bodyString = request.getBodyString();
    LoginAccountVo loginAccountVo = Json.getJson().parse(bodyString, LoginAccountVo.class);

    RespBodyVo respVo;
    // 1.登录
    LoginService loginService = Aop.get(LoginService.class);
    Long userId = loginService.getUserIdByUsernameAndPassword(loginAccountVo);

    if (userId != null) {

      // 2. 设置过期时间payload 7天
      long tokenTimeout = (System.currentTimeMillis() + 3600000 * 24 * 7) / 1000;

      // 3.创建token
      String keyValue = EnvUtils.getStr(AppConstant.ADMIN_SECRET_KEY);
      AuthToken authToken = JwtUtils.createToken(keyValue, new AuthToken(userId, tokenTimeout));
      TokenManager.login(userId, authToken.getToken());

      Kv kv = new Kv();
      kv.set("userId", userId);
      kv.set("token", authToken.getToken());
      kv.set("tokenTimeout", tokenTimeout);
      kv.set("type", loginAccountVo.getType());
      kv.set("status", "ok");

      respVo = RespBodyVo.ok(kv);
    } else {
      Map<String, String> data = new HashMap<>(1);
      data.put("status", "false");
      respVo = RespBodyVo.fail().data(data);

    }

    return Resps.json(httpResponse, respVo);
  }

  public HttpResponse outLogin(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());
    Long userIdLong = TioRequestContext.getUserIdLong();
    //remove
    TokenManager.logout(userIdLong);

    return Resps.json(httpResponse, RespBodyVo.ok());
  }

  /**
   * 因为拦击器已经经过了验证,判断token是否存在即可
   * @param request
   * @return
   */
  public HttpResponse validateLogin(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    Long userIdLong = TioRequestContext.getUserIdLong();
    boolean login = TokenManager.isLogin(userIdLong);
    return Resps.json(httpResponse, RespBodyVo.ok(login));

  }
}