package com.litongjava.tio.boot.admin.config;

import com.litongjava.tio.boot.http.interceptor.HttpInteceptorConfigure;
import com.litongjava.tio.boot.http.interceptor.HttpInterceptorModel;
import com.litongjava.tio.boot.satoken.AuthTokenInterceptor;
import com.litongjava.tio.boot.server.TioBootServer;

public class TioAdminInterceptorConfiguration {

  private String[] permitUrls;
  private boolean alloweStaticFile;

  public TioAdminInterceptorConfiguration() {
  }

  public TioAdminInterceptorConfiguration(String[] permitUrls) {
    this.permitUrls = permitUrls;
  }

  public TioAdminInterceptorConfiguration(String[] permitUrls, boolean b) {
    this.permitUrls = permitUrls;
    this.alloweStaticFile = b;
  }

  public void config() {
    // 创建 SaToken 拦截器实例
    AuthTokenInterceptor authTokenInterceptor = new AuthTokenInterceptor();
    HttpInterceptorModel model = new HttpInterceptorModel();
    model.setInterceptor(authTokenInterceptor);
    model.addBlockUrl("/**"); // 拦截所有路由
    // index
    model.addAllowUrls("", "/");
    //user
    model.addAllowUrls("/register/*", "/api/login/account", "/api/login/outLogin"); // 设置例外路由
    model.addAllowUrls("/api/event/add");

    String[] previewUrls = { "/table/json/tio_boot_admin_system_article/get/*", "/table/json/tio_boot_admin_system_docx/get/*", "/table/json/tio_boot_admin_system_pdf/get/*" };

    model.addAllowUrls(previewUrls);
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
