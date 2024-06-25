package com.litongjava.tio.boot.admin.config;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.aop.annotation.AConfiguration;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.tio.boot.admin.handler.ApiLoginHandler;
import com.litongjava.tio.boot.admin.handler.FakeAnalysisChartDataHandler;
import com.litongjava.tio.boot.admin.handler.GeographicHandler;
import com.litongjava.tio.boot.admin.handler.StableDiffusionHandler;
import com.litongjava.tio.boot.admin.handler.SystemFileHandler;
import com.litongjava.tio.boot.admin.handler.SystemHandler;
import com.litongjava.tio.boot.admin.handler.UserEventHandler;
import com.litongjava.tio.boot.admin.handler.UserHandler;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.http.server.router.HttpReqeustSimpleHandlerRoute;

@AConfiguration
public class HttpRequestHandlerConfig {

  @AInitialization
  public void httpRoutes() {
    // 创建simpleHttpRoutes
    HttpReqeustSimpleHandlerRoute r = TioBootServer.me().getHttpReqeustSimpleHandlerRoute();

    // 创建controller
    ApiLoginHandler apiLoginHandler = Aop.get(ApiLoginHandler.class);
    UserEventHandler userEventHandler = Aop.get(UserEventHandler.class);
    UserHandler userHandler = Aop.get(UserHandler.class);
    SystemFileHandler systemUploadHandler = Aop.get(SystemFileHandler.class);
    FakeAnalysisChartDataHandler fakeAnalysisChartDataHandler = Aop.get(FakeAnalysisChartDataHandler.class);
    GeographicHandler geographicHandler = Aop.get(GeographicHandler.class);
    SystemHandler systemHandler = Aop.get(SystemHandler.class);
    StableDiffusionHandler stableDiffusionHandler = Aop.get(StableDiffusionHandler.class);
    // 添加action
    r.add("/api/login/account", apiLoginHandler::account);
    r.add("/api/login/outLogin", apiLoginHandler::outLogin);
    r.add("/api/login/validateLogin", apiLoginHandler::validateLogin);
    r.add("/api/currentUser", userHandler::currentUser);
    r.add("/api/accountSettingCurrentUser", userHandler::accountSettingCurrentUser);
    r.add("/api/event/add", userEventHandler::add);
    r.add("/api/fake_analysis_chart_data", fakeAnalysisChartDataHandler::index);
    // upload
    r.add("/api/system/file/upload", systemUploadHandler::upload);
    r.add("/api/system/file/uploadImageToGoogle", systemUploadHandler::uploadImageToGoogle);
    r.add("/api/system/file/uploadToTencentCos", systemUploadHandler::uploadToTencentCos);
    r.add("/api/system/file/s3/upload", systemUploadHandler::uploadToS3);
    r.add("/api/system/file/s3/md5", systemUploadHandler::getFromS3ByMd5);
    r.add("/api/system/file/s3/url", systemUploadHandler::getUrlFromS3);

    r.add("/api/system/file/getGoogleFileUrl", systemUploadHandler::getGoogleFileUrl);
    r.add("/api/system/changeUserPassword", systemHandler::changeUserPassword);
    r.add("/api/geographic/province", geographicHandler::province);
    r.add("/api/sd/generateSd3", stableDiffusionHandler::generateSd3);
  }
}
