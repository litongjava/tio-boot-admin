package com.litongjava.tio.boot.admin.config;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.jfinal.aop.annotation.BeforeStartConfiguration;
import com.litongjava.tio.boot.admin.handler.ApiLoginHandler;
import com.litongjava.tio.boot.admin.handler.FakeAnalysisChartDataHandler;
import com.litongjava.tio.boot.admin.handler.UserEventHandler;
import com.litongjava.tio.boot.admin.handler.UserHandler;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.http.server.handler.SimpleHttpRoutes;

@BeforeStartConfiguration
public class HttpServerRequestHanlderConfig {

  @AInitialization
  public void httpRoutes() {

    // 创建simpleHttpRoutes
    SimpleHttpRoutes r = new SimpleHttpRoutes();
    // 创建controller
    ApiLoginHandler apiLoginHandler = Aop.get(ApiLoginHandler.class);
    UserEventHandler userEventHandler = Aop.get(UserEventHandler.class);
    UserHandler userHandler = Aop.get(UserHandler.class);
    FakeAnalysisChartDataHandler fakeAnalysisChartDataHandler = Aop.get(FakeAnalysisChartDataHandler.class);
    // 添加action
    r.add("/api/login/account", apiLoginHandler::account);
    r.add("/api/currentUser", userHandler::currentUser);
    r.add("/api/event/add", userEventHandler::add);
    r.add("/api/fake_analysis_chart_data", fakeAnalysisChartDataHandler::index);

    // 将simpleHttpRoutes添加到TioBootServer
    TioBootServer.me().setHttpRoutes(r);
  }
}
