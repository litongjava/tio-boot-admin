package com.litongjava.tio.boot.admin.utils;

import com.litongjava.tio.boot.admin.costants.AppConstant;
import com.litongjava.tio.utils.environment.EnvUtils;

public class TioAdminEnvUtils {
  public static String getAdminSecretKey() {
    return EnvUtils.getStr(AppConstant.ADMIN_SECRET_KEY);
  }
}

