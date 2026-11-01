package nexus.io.tio.boot.extension.rocketmq;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.producer.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nexus.io.hook.HookCan;

/**
 * Entry point of the RocketMQ integration. Typical setup in a configuration class:
 *
 * <pre>
 * RocketMq.init(RocketMqProperties.fromEnv());
 * RocketMq.addSendMessageHook(new TenantSendMessageHook());
 * RocketMq.addConsumeMessageHook(new TenantConsumeMessageHook());
 * RocketMq.addListener(new OrderCreatedListener());
 * RocketMq.start();
 * </pre>
 *
 * The producer and all consumers are shut down when tio-boot stops.
 */
public final class RocketMq {

  private static final Logger log = LoggerFactory.getLogger(RocketMq.class);

  private static volatile RocketMqProperties properties;
  private static volatile RocketMqTemplate template;
  private static volatile RocketMqMessageConverter converter = new RocketMqMessageConverter();
  private static final List<RocketMqListenerContainer> containers = new CopyOnWriteArrayList<>();
  private static final List<SendMessageHook> sendHooks = new CopyOnWriteArrayList<>();
  private static final List<ConsumeMessageHook> consumeHooks = new CopyOnWriteArrayList<>();
  private static volatile boolean started;

  private RocketMq() {
  }

  /**
   * Initializes with settings from app.properties / environment.
   */
  public static synchronized void init() {
    init(RocketMqProperties.fromEnv());
  }

  /**
   * Initializes with the given settings. A producer is created when {@code producerGroup} is set.
   */
  public static synchronized void init(RocketMqProperties rocketMqProperties) {
    if (properties != null) {
      throw new RocketMqException("RocketMq has already been initialized, call RocketMq.close() first");
    }
    properties = rocketMqProperties;
    if (!rocketMqProperties.isAvailable()) {
      log.info("RocketMQ is disabled (rocketmq.enabled=false or rocketmq.name-server is empty)");
      return;
    }
    String group = rocketMqProperties.getProducerGroup();
    if (group != null && !group.trim().isEmpty()) {
      template = RocketMqTemplate.create(rocketMqProperties);
      for (SendMessageHook hook : sendHooks) {
        template.registerSendMessageHook(hook);
      }
    }
  }

  /**
   * Replaces the payload converter. Call before adding listeners.
   */
  public static void setConverter(RocketMqMessageConverter messageConverter) {
    converter = messageConverter;
  }

  public static RocketMqMessageConverter getConverter() {
    return converter;
  }

  /**
   * Whether RocketMQ is initialized, switched on and has a name server.
   */
  public static boolean isEnabled() {
    RocketMqProperties p = properties;
    return p != null && p.isAvailable();
  }

  public static RocketMqProperties getProperties() {
    return properties;
  }

  public static synchronized void addSendMessageHook(SendMessageHook hook) {
    sendHooks.add(hook);
    if (template != null) {
      template.registerSendMessageHook(hook);
    }
  }

  /**
   * Adds a hook applied to every listener added afterwards and to those not yet started.
   */
  public static synchronized void addConsumeMessageHook(ConsumeMessageHook hook) {
    consumeHooks.add(hook);
    for (RocketMqListenerContainer container : containers) {
      if (!container.isStarted()) {
        container.registerConsumeMessageHook(hook);
      }
    }
  }

  /**
   * Adds a listener whose class is annotated with {@link RocketMqMessageListener}.
   */
  public static RocketMqListenerContainer addListener(RocketMqListener<?> listener) {
    RocketMqMessageListener annotation = listener.getClass().getAnnotation(RocketMqMessageListener.class);
    if (annotation == null) {
      throw new RocketMqException(listener.getClass().getName() + " is not annotated with @RocketMqMessageListener");
    }
    return addListener(RocketMqConsumerOptions.from(annotation), listener, null);
  }

  public static RocketMqListenerContainer addListener(RocketMqConsumerOptions options, RocketMqListener<?> listener) {
    return addListener(options, listener, null);
  }

  /**
   * Adds a listener with explicit options and payload type. The payload type is required for lambdas.
   *
   * @return the container, or null when RocketMQ or this listener is switched off
   */
  public static synchronized RocketMqListenerContainer addListener(RocketMqConsumerOptions options,
      RocketMqListener<?> listener, Type payloadType) {
    if (properties == null) {
      throw new RocketMqException("Call RocketMq.init() before adding listeners");
    }
    if (!properties.isAvailable()) {
      log.info("RocketMQ is disabled, listener skipped: {}", listener.getClass().getName());
      return null;
    }
    if (!properties.isListenerEnabled(options.getConsumerGroup(), options.getTopic())) {
      log.info("RocketMQ listener switched off by rocketmq.consumer.listeners.{}.{}=false", options.getConsumerGroup(),
          options.getTopic());
      return null;
    }
    RocketMqListenerContainer container = new RocketMqListenerContainer(properties, options, listener, payloadType,
        converter);
    for (ConsumeMessageHook hook : consumeHooks) {
      container.registerConsumeMessageHook(hook);
    }
    containers.add(container);
    if (started) {
      container.start();
    }
    return container;
  }

  /**
   * Starts the producer and all listeners, and registers shutdown on application stop.
   * Listeners added after this call start immediately.
   */
  public static synchronized void start() {
    if (properties == null) {
      throw new RocketMqException("Call RocketMq.init() before RocketMq.start()");
    }
    if (started || !properties.isAvailable()) {
      return;
    }
    if (template != null) {
      template.start();
    }
    for (RocketMqListenerContainer container : containers) {
      container.start();
    }
    started = true;
    HookCan.me().addDestroyMethod(RocketMq::close);
  }

  /**
   * Stops all listeners and the producer, and clears the state so that {@link #init} can run again.
   */
  public static synchronized void close() {
    for (RocketMqListenerContainer container : containers) {
      try {
        container.stop();
      } catch (Exception e) {
        log.error("Failed to stop RocketMQ consumer {}", container.getOptions().getConsumerGroup(), e);
      }
    }
    containers.clear();
    if (template != null) {
      template.destroy();
    }
    template = null;
    properties = null;
    started = false;
    sendHooks.clear();
    consumeHooks.clear();
  }

  public static boolean isStarted() {
    return started;
  }

  public static List<RocketMqListenerContainer> getContainers() {
    return Collections.unmodifiableList(new ArrayList<>(containers));
  }

  /**
   * The producer template. Throws when no producer group is configured or RocketMQ is disabled.
   */
  public static RocketMqTemplate getTemplate() {
    RocketMqTemplate t = template;
    if (t == null) {
      throw new RocketMqException("RocketMQ producer is not available, check rocketmq.enabled, "
          + "rocketmq.name-server and rocketmq.producer.group");
    }
    return t;
  }

  /**
   * Synchronous send through the default template.
   */
  public static SendResult send(String destination, Object payload) {
    return getTemplate().send(destination, payload);
  }

  /**
   * Synchronous send with a business key through the default template.
   */
  public static SendResult send(String destination, Object payload, Object keys) {
    return getTemplate().send(destination, payload, keys);
  }
}
