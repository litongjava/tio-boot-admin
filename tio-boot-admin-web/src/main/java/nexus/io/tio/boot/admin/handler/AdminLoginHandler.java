package nexus.io.tio.boot.admin.handler;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.kit.Kv;

import nexus.io.jfinal.aop.Aop;
import nexus.io.model.body.RespBodyVo;
import nexus.io.model.token.AuthToken;
import nexus.io.tio.boot.admin.services.LoginService;
import nexus.io.tio.boot.admin.utils.TioAdminEnvUtils;
import nexus.io.tio.boot.admin.vo.LoginAccountVo;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;
import nexus.io.tio.http.server.model.HttpCors;
import nexus.io.tio.http.server.util.CORSUtils;
import nexus.io.tio.http.server.util.Resps;
import nexus.io.tio.utils.json.Json;
import nexus.io.tio.utils.jwt.JwtUtils;
import nexus.io.tio.utils.token.TokenManager;

public class AdminLoginHandler {
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
      String keyValue = TioAdminEnvUtils.getAdminSecretKey();
      AuthToken authToken = JwtUtils.createToken(keyValue, new AuthToken(userId.toString(), tokenTimeout));
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

  /**
   * @param request
   * @return
   */
  public HttpResponse validateToken(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());

    Long userIdLong = TioRequestContext.getUserIdLong();
    return Resps.txt(httpResponse, userIdLong.toString());
  }
}