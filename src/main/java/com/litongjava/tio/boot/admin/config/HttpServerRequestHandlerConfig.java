package com.litongjava.tio.boot.admin.config;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.jfinal.aop.annotation.BeforeStartConfiguration;
import com.litongjava.tio.boot.admin.handler.*;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.http.server.handler.SimpleHttpRoutes;

@BeforeStartConfiguration
public class HttpServerRequestHandlerConfig {

  @AInitialization
  public void httpRoutes() {

    // 创建simpleHttpRoutes
    SimpleHttpRoutes r = new SimpleHttpRoutes();
    // 创建controller
    ApiLoginHandler apiLoginHandler = Aop.get(ApiLoginHandler.class);
    UserEventHandler userEventHandler = Aop.get(UserEventHandler.class);
    UserHandler userHandler = Aop.get(UserHandler.class);
    SystemFileHandler systemUploadHandler = Aop.get(SystemFileHandler.class);
    FakeAnalysisChartDataHandler fakeAnalysisChartDataHandler = Aop.get(FakeAnalysisChartDataHandler.class);
    GeographicHandler geographicHandler = Aop.get(GeographicHandler.class);
    SystemHandler systemHandler = Aop.get(SystemHandler.class);
    // 添加action
    r.add("/api/login/account", apiLoginHandler::account);
    r.add("/api/login/outLogin", apiLoginHandler::outLogin);
    r.add("/api/login/validateLogin", apiLoginHandler::validateLogin);
    r.add("/api/currentUser", userHandler::currentUser);
    r.add("/api/accountSettingCurrentUser", userHandler::accountSettingCurrentUser);
    r.add("/api/event/add", userEventHandler::add);
    r.add("/api/fake_analysis_chart_data", fakeAnalysisChartDataHandler::index);
    //upload
    r.add("/api/system/file/upload", systemUploadHandler::upload);
    r.add("/api/system/file/uploadImageToGoogle", systemUploadHandler::uploadImageToGoogle);
    r.add("/api/system/file/getGoogleFileUrl", systemUploadHandler::getGoogleFileUrl);
    r.add("/api/system/changeUserPassword", systemHandler::changeUserPassword);
    r.add("/api/geographic/province", geographicHandler::province);
    // 将simpleHttpRoutes添加到TioBootServer
    TioBootServer.me().setHttpRoutes(r);
  }
}
