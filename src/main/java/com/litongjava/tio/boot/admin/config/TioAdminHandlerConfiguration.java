package com.litongjava.tio.boot.admin.config;

import com.litongjava.tio.boot.admin.handler.AdminLoginHandler;
import com.litongjava.tio.boot.admin.handler.AdminUserHandler;
import com.litongjava.tio.boot.admin.handler.AppPreflightHandler;
import com.litongjava.tio.boot.admin.handler.AppUserAnonymousHandler;
import com.litongjava.tio.boot.admin.handler.AppUserGoogleHandler;
import com.litongjava.tio.boot.admin.handler.AppUserHandler;
import com.litongjava.tio.boot.admin.handler.AppUserLoginHandler;
import com.litongjava.tio.boot.admin.handler.AppUserRegisterHandler;
import com.litongjava.tio.boot.admin.handler.EmailVerificationHandler;
import com.litongjava.tio.boot.admin.handler.FakeAnalysisChartDataHandler;
import com.litongjava.tio.boot.admin.handler.GeographicHandler;
import com.litongjava.tio.boot.admin.handler.StableDiffusionHandler;
import com.litongjava.tio.boot.admin.handler.UserEventHandler;
import com.litongjava.tio.boot.admin.handler.system.SystemFileAliyunOssHandler;
import com.litongjava.tio.boot.admin.handler.system.SystemFileAwsS3Handler;
import com.litongjava.tio.boot.admin.handler.system.SystemFileFirebaseHandler;
import com.litongjava.tio.boot.admin.handler.system.SystemFileTencentCosHandler;
import com.litongjava.tio.boot.admin.handler.system.SystemUserHandler;
import com.litongjava.tio.boot.server.TioBootServer;
import com.litongjava.tio.http.server.router.HttpRequestRouter;

public class TioAdminHandlerConfiguration {

  public void config() {
    HttpRequestRouter r = TioBootServer.me().getRequestRouter();
    if (r == null) {
      return;
    }
    // 创建controller
    AdminLoginHandler apiLoginHandler = new AdminLoginHandler();
    UserEventHandler userEventHandler = new UserEventHandler();
    AdminUserHandler userHandler = new AdminUserHandler();

    FakeAnalysisChartDataHandler fakeAnalysisChartDataHandler = new FakeAnalysisChartDataHandler();
    GeographicHandler geographicHandler = new GeographicHandler();
    SystemUserHandler systemHandler = new SystemUserHandler();
    StableDiffusionHandler stableDiffusionHandler = new StableDiffusionHandler();
    SystemFileAwsS3Handler systemFileS3Handler = new SystemFileAwsS3Handler();

    AppPreflightHandler appPreflightHandler = new AppPreflightHandler();
    r.add("/preflight", appPreflightHandler);
    // 添加action
    r.add("/api/login/account", apiLoginHandler::account);
    r.add("/api/login/outLogin", apiLoginHandler::outLogin);
    r.add("/api/login/validateLogin", apiLoginHandler::validateLogin);
    r.add("/api/currentUser", userHandler::currentUser);
    r.add("/api/accountSettingCurrentUser", userHandler::accountSettingCurrentUser);
    r.add("/api/event/add", userEventHandler::add);
    r.add("/api/fake_analysis_chart_data", fakeAnalysisChartDataHandler::index);
    // upload
    // r.add("/api/system/file/upload", systemUploadHandler::upload);
    // r.add("/api/system/file/url", systemUploadHandler::getUrl);

    r.add("/api/system/file/s3/upload", systemFileS3Handler::upload);
    r.add("/api/system/file/s3/md5", systemFileS3Handler::getUploadRecordByMd5);
    r.add("/api/system/file/s3/url", systemFileS3Handler::getUrl);

    SystemFileTencentCosHandler systemFileTencentCosHandler = new SystemFileTencentCosHandler();
    r.add("/api/system/file/cos/upload", systemFileTencentCosHandler::upload);
    r.add("/api/system/file/cos/md5", systemFileTencentCosHandler::getUploadRecordByMd5);
    r.add("/api/system/file/cos/url", systemFileTencentCosHandler::getUrl);
    
    SystemFileAliyunOssHandler systemFileAliyunOssHandler = new SystemFileAliyunOssHandler();
    r.add("/api/system/file/oss/upload", systemFileAliyunOssHandler::upload);
    r.add("/api/system/file/oss/md5", systemFileAliyunOssHandler::getUploadRecordByMd5);
    r.add("/api/system/file/oss/url", systemFileAliyunOssHandler::getUrl);

    SystemFileFirebaseHandler systemFileFirebaseHandler = new SystemFileFirebaseHandler();
    r.add("/api/system/file/firebase/upload", systemFileFirebaseHandler::upload);
    r.add("/api/system/file/firebase/getUrl", systemFileFirebaseHandler::getUrl);

    r.add("/api/system/changeUserPassword", systemHandler::changeUserPassword);
    r.add("/api/geographic/province", geographicHandler::province);
    r.add("/api/sd/generateSd3", stableDiffusionHandler::generateSd3);

    AppUserRegisterHandler appUserRegisterHandler = new AppUserRegisterHandler();
    AppUserLoginHandler loginHandler = new AppUserLoginHandler();
    EmailVerificationHandler emailVerificationHandler = new EmailVerificationHandler();
    AppUserHandler appUserHandler = new AppUserHandler();

    AppUserAnonymousHandler appUserAnonymousHandler = new AppUserAnonymousHandler();
    // 注册接口
    r.add("/api/v1/register", appUserRegisterHandler::register);
    // 登录接口
    r.add("/api/v1/login", loginHandler::login);

    // 登出
    r.add("/api/v1/logout", loginHandler::logout);
    // 刷新
    r.add("/api/v1/user/refresh", appUserHandler::refresh);
    // 删除
    r.add("/api/v1/user/remove", appUserHandler::remove);

    r.add("/api/v1/user/profile", appUserHandler::profile);
    r.add("/api/v1/user/update", appUserHandler::update);
    r.add("/api/v1/user/updatePassword", appUserHandler::updatePassword);

    // 删除
    r.add("/api/v1/user/resetPassword", appUserHandler::resetPassword);
    r.add("/api/v1/anonymous/create", appUserAnonymousHandler::create);

    // 发送验证码邮件接口
    r.add("/api/v1/sendVerification", emailVerificationHandler::sendVerification);
    r.add("/api/v1/sendVerificationCode", emailVerificationHandler::sendVerificationCode);
    // 邮箱验证接口
    r.add("/api/v1/verify", emailVerificationHandler::verifyEmail);
    r.add("/verification/email", emailVerificationHandler::verifyEmail);

    AppUserGoogleHandler appUserGoogleHandler = new AppUserGoogleHandler();
    r.add("/api/v1/google/login", appUserGoogleHandler::login);
  }
}
