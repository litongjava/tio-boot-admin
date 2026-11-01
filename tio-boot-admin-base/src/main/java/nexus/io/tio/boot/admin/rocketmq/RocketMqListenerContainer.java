package nexus.io.tio.boot.admin.rocketmq;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.RPCHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps one {@link DefaultMQPushConsumer} and dispatches every received message to a
 * {@link RocketMqListener}, converting the body to the listener's payload type.
 */
public class RocketMqListenerContainer {

  private static final Logger log = LoggerFactory.getLogger(RocketMqListenerContainer.class);

  private final RocketMqConsumerOptions options;
  private final RocketMqListener<Object> listener;
  private final Type payloadType;
  private final RocketMqMessageConverter converter;
  private final DefaultMQPushConsumer consumer;
  private volatile boolean started;

  @SuppressWarnings("unchecked")
  public RocketMqListenerContainer(RocketMqProperties properties, RocketMqConsumerOptions options,
      RocketMqListener<?> listener, Type payloadType, RocketMqMessageConverter converter) {
    if (options.getTopic() == null || options.getTopic().isEmpty()) {
      throw new RocketMqException("Listener topic is required: " + listener.getClass().getName());
    }
    if (options.getConsumerGroup() == null || options.getConsumerGroup().isEmpty()) {
      throw new RocketMqException("Listener consumerGroup is required: " + listener.getClass().getName());
    }
    this.options = options;
    this.listener = (RocketMqListener<Object>) listener;
    this.payloadType = payloadType != null ? payloadType : resolvePayloadType(listener.getClass());
    this.converter = converter;
    this.consumer = createConsumer(properties, options);
  }

  private DefaultMQPushConsumer createConsumer(RocketMqProperties properties, RocketMqConsumerOptions options) {
    String accessKey = options.getAccessKey() != null ? options.getAccessKey() : properties.getConsumerAccessKey();
    String secretKey = options.getSecretKey() != null ? options.getSecretKey() : properties.getConsumerSecretKey();
    boolean trace = options.getEnableMsgTrace() != null ? options.getEnableMsgTrace()
        : properties.isConsumerEnableMsgTrace();
    String traceTopic = options.getCustomizedTraceTopic() != null ? options.getCustomizedTraceTopic()
        : properties.getConsumerCustomizedTraceTopic();
    String nameServer = options.getNameServer() != null ? options.getNameServer() : properties.getNameServer();

    RPCHook rpcHook = RocketMqTemplate.aclHook(accessKey, secretKey);
    DefaultMQPushConsumer c = new DefaultMQPushConsumer(options.getConsumerGroup(), rpcHook,
        new AllocateMessageQueueAveragely(), trace, traceTopic);
    c.setNamesrvAddr(nameServer);
    c.setInstanceName(UtilAll.getPid() + "#" + options.getConsumerGroup() + "#" + System.nanoTime());
    c.setVipChannelEnabled(properties.isVipChannelEnabled());
    c.setUseTLS(properties.isUseTls());
    c.setMessageModel(options.getMessageModel());
    c.setConsumeThreadMax(options.getConsumeThreadNumber());
    if (c.getConsumeThreadMin() > options.getConsumeThreadNumber()) {
      c.setConsumeThreadMin(options.getConsumeThreadNumber());
    }
    c.setConsumeTimeout(options.getConsumeTimeout());
    c.setMaxReconsumeTimes(options.getMaxReconsumeTimes());
    c.setSuspendCurrentQueueTimeMillis(options.getSuspendCurrentQueueTimeMillis());
    try {
      if (options.getSelectorType() == RocketMqSelectorType.SQL92) {
        c.subscribe(options.getTopic(), MessageSelector.bySql(options.getSelectorExpression()));
      } else {
        c.subscribe(options.getTopic(), MessageSelector.byTag(options.getSelectorExpression()));
      }
    } catch (Exception e) {
      throw new RocketMqException("Failed to subscribe topic " + options.getTopic(), e);
    }
    if (options.getConsumeMode() == RocketMqConsumeMode.ORDERLY) {
      c.registerMessageListener(new OrderlyListener());
    } else {
      c.registerMessageListener(new ConcurrentlyListener());
    }
    return c;
  }

  /**
   * Registers a hook that runs before and after every consume call, for example to restore the
   * tenant id from message properties into a thread local. Call before {@link #start()}.
   */
  public RocketMqListenerContainer registerConsumeMessageHook(ConsumeMessageHook hook) {
    consumer.getDefaultMQPushConsumerImpl().registerConsumeMessageHook(hook);
    return this;
  }

  public synchronized void start() {
    if (started) {
      return;
    }
    try {
      consumer.start();
      started = true;
      log.info("RocketMQ consumer started, topic={}, group={}, model={}, mode={}, listener={}", options.getTopic(),
          options.getConsumerGroup(), options.getMessageModel(), options.getConsumeMode(),
          listener.getClass().getName());
    } catch (Exception e) {
      throw new RocketMqException("Failed to start RocketMQ consumer, group=" + options.getConsumerGroup(), e);
    }
  }

  public synchronized void stop() {
    if (started) {
      consumer.shutdown();
      started = false;
      log.info("RocketMQ consumer stopped, topic={}, group={}", options.getTopic(), options.getConsumerGroup());
    }
  }

  public boolean isStarted() {
    return started;
  }

  public DefaultMQPushConsumer getConsumer() {
    return consumer;
  }

  public RocketMqConsumerOptions getOptions() {
    return options;
  }

  public RocketMqListener<?> getListener() {
    return listener;
  }

  public Type getPayloadType() {
    return payloadType;
  }

  private void handle(MessageExt message) {
    Object payload = converter.fromMessage(message, payloadType);
    listener.onMessage(payload);
  }

  private class ConcurrentlyListener implements MessageListenerConcurrently {
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
      for (MessageExt message : messages) {
        try {
          handle(message);
        } catch (Throwable e) {
          log.warn("Consume failed, will retry. topic={}, group={}, msgId={}, reconsumeTimes={}", message.getTopic(),
              options.getConsumerGroup(), message.getMsgId(), message.getReconsumeTimes(), e);
          context.setDelayLevelWhenNextConsume(options.getDelayLevelWhenNextConsume());
          return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
      }
      return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
  }

  private class OrderlyListener implements MessageListenerOrderly {
    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> messages, ConsumeOrderlyContext context) {
      for (MessageExt message : messages) {
        try {
          handle(message);
        } catch (Throwable e) {
          log.warn("Orderly consume failed, queue suspended. topic={}, group={}, msgId={}", message.getTopic(),
              options.getConsumerGroup(), message.getMsgId(), e);
          context.setSuspendCurrentQueueTimeMillis(options.getSuspendCurrentQueueTimeMillis());
          return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
        }
      }
      return ConsumeOrderlyStatus.SUCCESS;
    }
  }

  /**
   * Finds T of {@code RocketMqListener<T>} in the class hierarchy of the listener.
   */
  public static Type resolvePayloadType(Class<?> listenerClass) {
    Class<?> current = listenerClass;
    while (current != null && current != Object.class) {
      for (Type itf : current.getGenericInterfaces()) {
        if (itf instanceof ParameterizedType) {
          ParameterizedType pt = (ParameterizedType) itf;
          if (pt.getRawType() == RocketMqListener.class) {
            Type arg = pt.getActualTypeArguments()[0];
            if (arg instanceof Class || arg instanceof ParameterizedType) {
              return arg;
            }
          }
        }
      }
      current = current.getSuperclass();
    }
    throw new RocketMqException("Cannot resolve the payload type of " + listenerClass.getName()
        + ", implement RocketMqListener<YourType> in a class or pass the payload type explicitly");
  }
}
