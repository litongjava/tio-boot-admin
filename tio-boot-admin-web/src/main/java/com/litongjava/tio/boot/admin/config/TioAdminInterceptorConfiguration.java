package com.litongjava.tio.boot.admin.config;

import com.litongjava.tio.boot.admin.consts.TioBootAdminUrls;
import com.litongjava.tio.boot.admin.services.TioBootAdminTokenPredicate;

import nexus.io.tio.boot.http.interceptor.HttpInteceptorConfigure;
import nexus.io.tio.boot.http.interceptor.HttpInterceptorModel;
import nexus.io.tio.boot.server.TioBootServer;
import nexus.io.tio.boot.token.TokenPredicate;
import nexus.io.tio.boot.token.UserTokenInterceptor;

public class TioAdminInterceptorConfiguration {

  private String[] permitUrls;
  private boolean alloweStaticFile = true;
  private TokenPredicate validateTokenLogic;

  public TioAdminInterceptorConfiguration() {
  }

  public TioAdminInterceptorConfiguration(String[] permitUrls) {
    this.permitUrls = permitUrls;
  }

  public TioAdminInterceptorConfiguration(String[] permitUrls, TokenPredicate validateTokenLogic) {
    this.permitUrls = permitUrls;
    this.validateTokenLogic = validateTokenLogic;
  }

  public TioAdminInterceptorConfiguration(String[] permitUrls, boolean b) {
    this.permitUrls = permitUrls;
    this.alloweStaticFile = b;
  }

  public void config() {
    // token验证逻辑
    if (validateTokenLogic == null) {
      validateTokenLogic = new TioBootAdminTokenPredicate();
    }

    UserTokenInterceptor userTokenInterceptor = new UserTokenInterceptor(validateTokenLogic);
    HttpInterceptorModel model = new HttpInterceptorModel();
    model.setInterceptor(userTokenInterceptor);
    // 拦截所有路由
    model.addBlockUrl("/**");

    // 添加不拦截的路由
    model.addAllowUrls(TioBootAdminUrls.ALLLOW_URLS);

    if (permitUrls != null) {
      model.addAllowUrls(permitUrls);
    }
    model.setAlloweStaticFile(alloweStaticFile);

    HttpInteceptorConfigure inteceptorConfigure = new HttpInteceptorConfigure();
    inteceptorConfigure.add(model);
    // 将拦截器配置添加到 Tio 服务器
    TioBootServer.me().setHttpInteceptorConfigure(inteceptorConfigure);
  }
}
