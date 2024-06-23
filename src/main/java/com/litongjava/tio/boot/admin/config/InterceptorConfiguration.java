package com.litongjava.tio.boot.admin.config;
// 导入必要的类和注解

import com.litongjava.jfinal.aop.annotation.AConfiguration;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.tio.boot.http.interceptor.HttpServerInterceptorModel;
import com.litongjava.tio.boot.http.interceptor.ServerInteceptorConfigure;
import com.litongjava.tio.boot.satoken.SaTokenInterceptor;
import com.litongjava.tio.boot.server.TioBootServer;

@AConfiguration
public class InterceptorConfiguration {

  @AInitialization
  public void config() {
    // 创建 SaToken 拦截器实例
    SaTokenInterceptor saTokenInterceptor = new SaTokenInterceptor();
    HttpServerInterceptorModel model = new HttpServerInterceptorModel();
    model.setInterceptor(saTokenInterceptor);
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

    ServerInteceptorConfigure serverInteceptorConfigure = new ServerInteceptorConfigure();
    serverInteceptorConfigure.add(model);
    // 将拦截器配置添加到 Tio 服务器
    TioBootServer.me().setServerInteceptorConfigure(serverInteceptorConfigure);
  }
}
