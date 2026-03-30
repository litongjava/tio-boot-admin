package com.litongjava.tio.boot.admin.handler;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.model.body.RespBodyVo;
import com.litongjava.tio.boot.admin.services.AppUserGoogleService;
import com.litongjava.tio.boot.http.TioRequestContext;
import com.litongjava.tio.http.common.HttpRequest;
import com.litongjava.tio.http.common.HttpResponse;

public class AppUserGoogleHandler {
  /**
   * 处理 Google 登录回调
   * 前端将获取的授权码通过 query 参数传递过来
   */
  public HttpResponse login(HttpRequest request) {
    // 获取授权码参数
    String code = request.getParameter("code");
    String redirect_url = request.getParam("redirect_url");
    HttpResponse response = TioRequestContext.getResponse();

    // 获取 Google 登录服务实例
    AppUserGoogleService googleService = Aop.get(AppUserGoogleService.class);
    // 使用授权码处理登录流程
    RespBodyVo vo = googleService.login(code, redirect_url);
    return response.setJson(vo);
  }
}