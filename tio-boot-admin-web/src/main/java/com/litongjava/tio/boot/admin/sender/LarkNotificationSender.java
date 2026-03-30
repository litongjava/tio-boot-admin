package com.litongjava.tio.boot.admin.sender;
import com.litongjava.tio.boot.admin.utils.TioAdminEnvUtils;
import com.litongjava.tio.boot.admin.utils.TioVirtualThreadUtils;
import com.litongjava.tio.utils.notification.NotificationSender;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.notification.LarksuiteNotificationUtils;
import com.litongjava.tio.utils.notification.NotifactionWarmModel;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

@Slf4j
public class LarkNotificationSender implements NotificationSender {

  @Override
  public boolean send(NotifactionWarmModel model) {
    final String webHookUrl = TioAdminEnvUtils.getAppWarmNotificationWebhookUrl();
    if (webHookUrl != null) {
      try (Response response = LarksuiteNotificationUtils.sendWarm(webHookUrl, model)) {
        if (!response.isSuccessful()) {
          log.error("Faild to push :{}", response.body().string());
          return false;
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean sendAsync(NotifactionWarmModel model) {
    TioVirtualThreadUtils.submit(() -> {
      send(model);
    });
    return true;
  }
}