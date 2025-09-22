package com.litongjava.tio.boot.admin.config;

import java.util.function.Predicate;

import com.litongjava.tio.boot.admin.costants.TioBootAdminUrls;
import com.litongjava.tio.boot.admin.services.TioBootAdminTokenPredicate;
import com.litongjava.tio.boot.http.interceptor.HttpInteceptorConfigure;
import com.litongjava.tio.boot.http.interceptor.HttpInterceptorModel;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.boot.token.AuthTokenInterceptor;
import com.litongjava.tio.boot.token.UserTokenInterceptor;

public class TioAdminInterceptorConfiguration {

  private String[] permitUrls;
  private boolean alloweStaticFile = true;
  private Predicate<String> validateTokenLogic;

  public TioAdminInterceptorConfiguration() {
  }

  public TioAdminInterceptorConfiguration(String[] permitUrls) {
    this.permitUrls = permitUrls;
  }

  public TioAdminInterceptorConfiguration(String[] permitUrls, Predicate<String> validateTokenLogic) {
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

    AuthTokenInterceptor authTokenInterceptor = new AuthTokenInterceptor(validateTokenLogic);
    TioBootServer.me().setAuthTokenInterceptor(authTokenInterceptor);

    UserTokenInterceptor userTokenInterceptor = new UserTokenInterceptor();
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
