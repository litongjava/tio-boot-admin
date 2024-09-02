package com.litongjava.tio.boot.admin.handler;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.services.UserService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;
import com.litongjava.tio.http.server.model.HttpCors;
import com.litongjava.tio.http.server.util.HttpServerResponseUtils;
import com.litongjava.tio.http.server.util.Resps;
import com.litongjava.tio.utils.resp.RespVo;

import cn.dev33.satoken.stp.StpUtil;

/**
 * Created by Tong Li <https://github.com/litongjava>
 */
public class UserHandler {

  public HttpResponse currentUser(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    HttpServerResponseUtils.enableCORS(httpResponse, new HttpCors());
    Object loginId = StpUtil.getLoginId();
    RespVo respVo = Aop.get(UserService.class).currentUser(loginId);
    return Resps.json(httpResponse, respVo);
  }

  public HttpResponse accountSettingCurrentUser(HttpRequest request) {
    HttpResponse httpResponse = TioRequestContext.getResponse();
    HttpServerResponseUtils.enableCORS(httpResponse, new HttpCors());
    Object loginId = StpUtil.getLoginId();
    RespVo respVo = Aop.get(UserService.class).currentUser(loginId);
    return Resps.json(httpResponse, respVo);
  }
}
