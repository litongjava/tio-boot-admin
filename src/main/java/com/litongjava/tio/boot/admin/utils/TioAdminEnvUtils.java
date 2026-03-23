package com.litongjava.tio.boot.admin.utils;

import com.litongjava.tio.boot.admin.consts.StoragePlatformConst;
import com.litongjava.tio.boot.admin.costants.AppConstant;
import com.litongjava.tio.utils.environment.EnvUtils;

public class TioAdminEnvUtils {
  public static String getAdminSecretKey() {
    return EnvUtils.getStr(AppConstant.APP_ADMIN_SECRET_KEY);
  }

  public static String getAdminToken() {
    return EnvUtils.get(AppConstant.APP_ADMIN_TOKEN);
  }

  public static Long getTokenTimeout() {
    return EnvUtils.getLong(AppConstant.APP_TOKEN_TIMEOUT, 604800L);
  }

  public static String getStoragePlatform() {
    return EnvUtils.getStr(AppConstant.APP_STORAGE_PLATFORM, StoragePlatformConst.cloudflare_r2);
  }

}
