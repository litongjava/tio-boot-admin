package nexus.io.tio.boot.admin.sender;
import lombok.extern.slf4j.Slf4j;
import nexus.io.tio.boot.admin.utils.TioAdminEnvUtils;
import nexus.io.tio.boot.admin.utils.TioVirtualThreadUtils;
import nexus.io.tio.utils.environment.EnvUtils;
import nexus.io.tio.utils.notification.LarksuiteNotificationUtils;
import nexus.io.tio.utils.notification.NotifactionWarmModel;
import nexus.io.tio.utils.notification.NotificationSender;
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