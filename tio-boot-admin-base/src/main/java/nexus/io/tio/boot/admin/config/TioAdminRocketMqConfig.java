package nexus.io.tio.boot.admin.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.client.hook.SendMessageHook;

import lombok.extern.slf4j.Slf4j;
import nexus.io.tio.boot.admin.rocketmq.RocketMq;
import nexus.io.tio.boot.admin.rocketmq.RocketMqListener;
import nexus.io.tio.boot.admin.rocketmq.RocketMqProperties;

/**
 * Initializes RocketMQ from rocketmq.* settings, registers hooks and listeners and starts the
 * producer and consumers. Everything is shut down when the application stops.
 *
 * <pre>
 * new TioAdminRocketMqConfig()
 *     .addSendMessageHook(new TenantSendMessageHook())
 *     .addConsumeMessageHook(new TenantConsumeMessageHook())
 *     .addListener(new OrderCreatedListener())
 *     .config();
 * </pre>
 */
@Slf4j
public class TioAdminRocketMqConfig {

  private RocketMqProperties properties;
  private final List<RocketMqListener<?>> listeners = new ArrayList<>();
  private final List<SendMessageHook> sendHooks = new ArrayList<>();
  private final List<ConsumeMessageHook> consumeHooks = new ArrayList<>();

  public TioAdminRocketMqConfig() {
  }

  public TioAdminRocketMqConfig(RocketMqListener<?>... listeners) {
    Collections.addAll(this.listeners, listeners);
  }

  /**
   * Uses the given settings instead of reading rocketmq.* from the environment.
   */
  public TioAdminRocketMqConfig properties(RocketMqProperties properties) {
    this.properties = properties;
    return this;
  }

  /**
   * Adds a listener whose class is annotated with @RocketMqMessageListener.
   */
  public TioAdminRocketMqConfig addListener(RocketMqListener<?> listener) {
    listeners.add(listener);
    return this;
  }

  public TioAdminRocketMqConfig addSendMessageHook(SendMessageHook hook) {
    sendHooks.add(hook);
    return this;
  }

  public TioAdminRocketMqConfig addConsumeMessageHook(ConsumeMessageHook hook) {
    consumeHooks.add(hook);
    return this;
  }

  public void config() {
    RocketMqProperties p = properties != null ? properties : RocketMqProperties.fromEnv();
    RocketMq.init(p);
    if (!RocketMq.isEnabled()) {
      log.info("rocketmq.name-server is blank or rocketmq.enabled=false, skip RocketMQ initialization");
      return;
    }
    for (SendMessageHook hook : sendHooks) {
      RocketMq.addSendMessageHook(hook);
    }
    for (ConsumeMessageHook hook : consumeHooks) {
      RocketMq.addConsumeMessageHook(hook);
    }
    for (RocketMqListener<?> listener : listeners) {
      RocketMq.addListener(listener);
    }
    RocketMq.start();
  }
}
