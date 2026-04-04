package nexus.io.tio.boot.admin.handler;

import nexus.io.jfinal.aop.Aop;
import nexus.io.model.body.RespBodyVo;
import nexus.io.tio.boot.admin.services.AppUserGoogleService;
import nexus.io.tio.boot.http.TioRequestContext;
import nexus.io.tio.http.common.HttpRequest;
import nexus.io.tio.http.common.HttpResponse;

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