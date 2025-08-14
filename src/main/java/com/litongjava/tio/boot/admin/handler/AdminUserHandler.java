package com.litongjava.tio.boot.admin.handler;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.services.AdminUserService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.CORSUtils;
import com.litongjava.tio.http.server.util.Resps;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class AdminUserHandler {

  public HttpResponse currentUser(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());
    String userIdString = TioRequestContext.getUserIdString();
    if(userIdString==null) {
      httpResponse.setStatus(401);
      return httpResponse;
    }
    Long userId = Long.valueOf(userIdString);
    RespBodyVo respVo = Aop.get(AdminUserService.class).currentUser(userId);
    httpResponse.body(respVo);
    return httpResponse;
  }

  public HttpResponse accountSettingCurrentUser(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    CORSUtils.enableCORS(httpResponse, new HttpCors());
    Long userId = Long.valueOf(TioRequestContext.getUserIdLong());
    RespBodyVo respVo = Aop.get(AdminUserService.class).currentUser(userId);
    return Resps.json(httpResponse, respVo);
  }
}
