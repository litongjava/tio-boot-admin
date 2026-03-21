package com.litongjava.tio.boot.admin.utils;

import com.litongjava.tio.boot.admin.costants.AppConstant;
import com.litongjava.tio.utils.environment.EnvUtils;

public class TioAdminEnvUtils {
  public static String getAdminSecretKey() {
    return EnvUtils.getStr(AppConstant.ADMIN_SECRET_KEY);
  }

  public static String getAdminToken() {
    return EnvUtils.get(AppConstant.ADMIN_TOKEN);
  }
  
  public static Long getAppTokenTimeout() {
    return  EnvUtils.getLong(AppConstant.APP_TOKEN_TIMEOUT, 604800L);
  }
  
}
