package com.litongjava.tio.boot.admin.config;

import com.litongjava.jfinal.aop.Aop;
import com.litongjava.tio.boot.admin.handler.ApiLoginHandler;
import com.litongjava.tio.boot.admin.handler.AppUserAnonymousHandler;
import com.litongjava.tio.boot.admin.handler.AppUserGoogleHandler;
import com.litongjava.tio.boot.admin.handler.AppUserHandler;
import com.litongjava.tio.boot.admin.handler.AppUserLoginHandler;
import com.litongjava.tio.boot.admin.handler.AppUserRegisterHandler;
import com.litongjava.tio.boot.admin.handler.EmailVerificationHandler;
import com.litongjava.tio.boot.admin.handler.FakeAnalysisChartDataHandler;
import com.litongjava.tio.boot.admin.handler.GeographicHandler;
import com.litongjava.tio.boot.admin.handler.StableDiffusionHandler;
import com.litongjava.tio.boot.admin.handler.SystemFileAwsS3Handler;
import com.litongjava.tio.boot.admin.handler.SystemFileFirebaseHandler;
import com.litongjava.tio.boot.admin.handler.SystemHandler;
import com.litongjava.tio.boot.admin.handler.UserEventHandler;
import com.litongjava.tio.boot.admin.handler.UserHandler;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.http.server.router.HttpRequestRouter;

public class TioAdminHandlerConfiguration {

  public void config() {
    HttpRequestRouter r = TioBootServer.me().getRequestRouter();
    if (r == null) {
      return;
    }
    // 创建controller
    ApiLoginHandler apiLoginHandler = Aop.get(ApiLoginHandler.class);
    UserEventHandler userEventHandler = Aop.get(UserEventHandler.class);
    UserHandler userHandler = Aop.get(UserHandler.class);

    FakeAnalysisChartDataHandler fakeAnalysisChartDataHandler = Aop.get(FakeAnalysisChartDataHandler.class);
    GeographicHandler geographicHandler = Aop.get(GeographicHandler.class);
    SystemHandler systemHandler = Aop.get(SystemHandler.class);
    StableDiffusionHandler stableDiffusionHandler = Aop.get(StableDiffusionHandler.class);
    SystemFileAwsS3Handler systemFileS3Handler = Aop.get(SystemFileAwsS3Handler.class);

    // 添加action
    r.add("/api/login/account", apiLoginHandler::account);
    r.add("/api/login/outLogin", apiLoginHandler::outLogin);
    r.add("/api/login/validateLogin", apiLoginHandler::validateLogin);
    r.add("/api/currentUser", userHandler::currentUser);
    r.add("/api/accountSettingCurrentUser", userHandler::accountSettingCurrentUser);
    r.add("/api/event/add", userEventHandler::add);
    r.add("/api/fake_analysis_chart_data", fakeAnalysisChartDataHandler::index);
    // upload
    //r.add("/api/system/file/upload", systemUploadHandler::upload);
    //r.add("/api/system/file/url", systemUploadHandler::getUrl);

    r.add("/api/system/file/s3/upload", systemFileS3Handler::upload);
    r.add("/api/system/file/s3/md5", systemFileS3Handler::getUploadRecordByMd5);
    r.add("/api/system/file/s3/url", systemFileS3Handler::getUrl);

    SystemFileFirebaseHandler systemFileFirebaseHandler = Aop.get(SystemFileFirebaseHandler.class);
    r.add("/api/system/file/uploadToGoogle", systemFileFirebaseHandler::upload);
    r.add("/api/system/file/firebase/getUrl", systemFileFirebaseHandler::getUrl);

    r.add("/api/system/changeUserPassword", systemHandler::changeUserPassword);
    r.add("/api/geographic/province", geographicHandler::province);
    r.add("/api/sd/generateSd3", stableDiffusionHandler::generateSd3);

    AppUserRegisterHandler appUserRegisterHandler = Aop.get(AppUserRegisterHandler.class);
    AppUserLoginHandler loginHandler = Aop.get(AppUserLoginHandler.class);
    EmailVerificationHandler emailVerificationHandler = Aop.get(EmailVerificationHandler.class);
    AppUserHandler appUserHandler = Aop.get(AppUserHandler.class);

    AppUserAnonymousHandler appUserAnonymousHandler = Aop.get(AppUserAnonymousHandler.class);
    // 注册接口
    r.add("/api/v1/register", appUserRegisterHandler::register);
    // 登录接口
    r.add("/api/v1/login", loginHandler::login);

    // 登出
    r.add("/api/v1/logout", loginHandler::logout);
    //刷新
    r.add("/api/v1/user/referesh", appUserHandler::refresh);
    //删除
    r.add("/api/v1/user/remove", appUserHandler::remove);
    //删除
    r.add("/api/v1/user/resetPassword", appUserHandler::resetPassword);

    r.add("/api/v1/anonymous/create", appUserAnonymousHandler::create);
    
    // 发送验证码邮件接口
    r.add("/api/v1/sendVerification", emailVerificationHandler::sendVerification);
    r.add("/api/v1/sendVerificationCode", emailVerificationHandler::sendVerificationCode);
    // 邮箱验证接口
    r.add("/api/v1/verify", emailVerificationHandler::verifyEmail);
    r.add("/verification/email", emailVerificationHandler::verifyEmail);
    
    AppUserGoogleHandler appUserGoogleHandler = Aop.get(AppUserGoogleHandler.class);
    r.add("/api/v1/google/login", appUserGoogleHandler::login);
  }
}
