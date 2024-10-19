package com.litongjava.tio.boot.admin.config;

import com.litongjava.tio.boot.http.interceptor.HttpInteceptorConfigure;
import com.litongjava.tio.boot.http.interceptor.HttpInterceptorModel;
import com.litongjava.tio.boot.satoken.AuthTokenInterceptor;
import com.litongjava.tio.boot.server.TioBootServer;

public class TioBootAdminAppInterceptorConfiguration {

  public void config() {
    // 创建 SaToken 拦截器实例
    AuthTokenInterceptor authTokenInterceptor = new AuthTokenInterceptor();
    HttpInterceptorModel model = new HttpInterceptorModel();
    model.setInterceptor(authTokenInterceptor);
    model.addblockeUrl("/**"); // 拦截所有路由
    // index
    model.addAlloweUrls("", "/");
    //user
    model.addAlloweUrls("/register/*", "/api/login/account","/api/login/outLogin"); // 设置例外路由
    model.addAlloweUrls("/api/event/add");
    String[] previewUrls= {
        "/table/json/tio_boot_admin_system_article/get/*",
        "/table/json/tio_boot_admin_system_docx/get/*",
        "/table/json/tio_boot_admin_system_pdf/get/*"
    };
    model.addAlloweUrls(previewUrls);

    HttpInteceptorConfigure serverInteceptorConfigure = new HttpInteceptorConfigure();
    serverInteceptorConfigure.add(model);
    // 将拦截器配置添加到 Tio 服务器
    TioBootServer.me().setHttpInteceptorConfigure(serverInteceptorConfigure);
  }
}
