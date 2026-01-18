package com.litongjava.tio.boot.admin.costants;

public interface TioBootAdminUrls {

  String[] ALLLOW_URLS = { "", "/",

      "/preflight", "/api/event/add",

      "/register/*", "/api/login/account", "/api/login/outLogin",
      //
      "/api/v1/login", "/api/v1/register", "/api/v1/user/referesh",
      //
      "/api/v1/sendVerification", "/api/v1/sendVerificationCode", "/api/v1/verify", "/verification/email",
      //
      "/api/v1/user/resetPassword", "/api/v1/anonymous/create",
      //
      "/api/v1/google/login", "/api/v1/user/refresh",

      "/table/json/tio_boot_admin_system_article/get/*",
      //
      "/table/json/tio_boot_admin_system_docx/get/*", "/table/json/tio_boot_admin_system_pdf/get/*" };
}
