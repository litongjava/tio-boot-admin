package com.litongjava.tio.boot.admin.config;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.jfinal.aop.annotation.AConfiguration;
import com.litongjava.jfinal.aop.annotation.AInitialization;
import com.litongjava.tio.boot.admin.handler.ApiLoginHandler;
import com.litongjava.tio.boot.admin.handler.FakeAnalysisChartDataHandler;
import com.litongjava.tio.boot.admin.handler.GeographicHandler;
import com.litongjava.tio.boot.admin.handler.StableDiffusionHandler;
import com.litongjava.tio.boot.admin.handler.SystemFileFirebaseHandler;
import com.litongjava.tio.boot.admin.handler.SystemFileHandler;
import com.litongjava.tio.boot.admin.handler.SystemFileS3Handler;
import com.litongjava.tio.boot.admin.handler.SystemHandler;
import com.litongjava.tio.boot.admin.handler.UserEventHandler;
import com.litongjava.tio.boot.admin.handler.UserHandler;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.http.server.router.RequestRoute;

@AConfiguration
public class HttpRequestHandlerConfig {

  @AInitialization
  public void httpRoutes() {
    // 创建simpleHttpRoutes
    RequestRoute r = TioBootServer.me().getRequestRoute();

    // 创建controller
    ApiLoginHandler apiLoginHandler = Aop.get(ApiLoginHandler.class);
    UserEventHandler userEventHandler = Aop.get(UserEventHandler.class);
    UserHandler userHandler = Aop.get(UserHandler.class);
    SystemFileHandler systemUploadHandler = Aop.get(SystemFileHandler.class);
    FakeAnalysisChartDataHandler fakeAnalysisChartDataHandler = Aop.get(FakeAnalysisChartDataHandler.class);
    GeographicHandler geographicHandler = Aop.get(GeographicHandler.class);
    SystemHandler systemHandler = Aop.get(SystemHandler.class);
    StableDiffusionHandler stableDiffusionHandler = Aop.get(StableDiffusionHandler.class);
    SystemFileS3Handler systemFileS3Handler = Aop.get(SystemFileS3Handler.class);
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

    r.add("/api/system/file/uploadToTencentCos", systemUploadHandler::uploadToTencentCos);
    r.add("/api/system/file/s3/upload", systemFileS3Handler::upload);
    r.add("/api/system/file/s3/md5", systemFileS3Handler::getUploadRecordByMd5);
    r.add("/api/system/file/s3/url", systemFileS3Handler::getUrl);

    SystemFileFirebaseHandler systemFileFirebaseHandler = Aop.get(SystemFileFirebaseHandler.class);
    r.add("/api/system/file/uploadToGoogle", systemFileFirebaseHandler::upload);
    r.add("/api/system/file/firebase/getUrl", systemFileFirebaseHandler::getUrl);

    r.add("/api/system/changeUserPassword", systemHandler::changeUserPassword);
    r.add("/api/geographic/province", geographicHandler::province);
    r.add("/api/sd/generateSd3", stableDiffusionHandler::generateSd3);
  }
}
