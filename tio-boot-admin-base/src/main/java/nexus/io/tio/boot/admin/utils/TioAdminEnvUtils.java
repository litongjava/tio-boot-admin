package nexus.io.tio.boot.admin.utils;

import nexus.io.constants.ServerConfigKeys;
import nexus.io.tio.boot.admin.consts.AppConstant;
import nexus.io.tio.boot.admin.consts.StoragePlatformConst;
import nexus.io.tio.utils.environment.EnvUtils;

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

  public static String getAppName() {
    return EnvUtils.getStr(ServerConfigKeys.APP_NAME, "tio-boot");
  }

  public static int getAreaCode() {
    return EnvUtils.getInt(AppConstant.APP_AREA_CODE);
  }

  public static boolean isChina() {
    int app_area_code = getAreaCode();
    return 86 == app_area_code;
  }

  public static String getAppWarmNotificationWebhookUrl() {
    return EnvUtils.getStr(AppConstant.APP_WARM_NOTIFICATION_WEBHOOK_URL);
  }

}
