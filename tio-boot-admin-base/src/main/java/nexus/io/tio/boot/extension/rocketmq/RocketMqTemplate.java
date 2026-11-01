package nexus.io.tio.boot.extension.rocketmq;

import java.util.Map;

import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.RPCHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Producer facade over {@link DefaultMQProducer}.
 * <p>
 * The destination is {@code topic} or {@code topic:tags}. The payload is either any object (sent as
 * JSON, String as UTF-8 text, byte[] as is) or a {@link RocketMqMessage} carrying keys, tags,
 * user properties and delay settings.
 */
public class RocketMqTemplate {

  private static final Logger log = LoggerFactory.getLogger(RocketMqTemplate.class);

  private final DefaultMQProducer producer;
  private final RocketMqMessageConverter converter;
  private final SelectMessageQueueByHash hashSelector = new SelectMessageQueueByHash();
  private volatile boolean started;

  public RocketMqTemplate(DefaultMQProducer producer) {
    this(producer, new RocketMqMessageConverter());
  }

  public RocketMqTemplate(DefaultMQProducer producer, RocketMqMessageConverter converter) {
    this.producer = producer;
    this.converter = converter;
  }

  /**
   * Creates a producer from the given properties. Call {@link #start()} before sending.
   */
  public static RocketMqTemplate create(RocketMqProperties properties) {
    if (properties.getProducerGroup() == null || properties.getProducerGroup().trim().isEmpty()) {
      throw new RocketMqException("rocketmq.producer.group is required");
    }
    RPCHook rpcHook = aclHook(properties.getProducerAccessKey(), properties.getProducerSecretKey());
    DefaultMQProducer producer = new DefaultMQProducer(properties.getProducerGroup(), rpcHook,
        properties.isProducerEnableMsgTrace(), properties.getProducerCustomizedTraceTopic());
    producer.setNamesrvAddr(properties.getNameServer());
    producer.setInstanceName(UtilAll.getPid() + "#producer#" + System.nanoTime());
    producer.setVipChannelEnabled(properties.isVipChannelEnabled());
    producer.setUseTLS(properties.isUseTls());
    producer.setSendMsgTimeout(properties.getSendMessageTimeout());
    producer.setCompressMsgBodyOverHowmuch(properties.getCompressMessageBodyThreshold());
    producer.setMaxMessageSize(properties.getMaxMessageSize());
    producer.setRetryTimesWhenSendFailed(properties.getRetryTimesWhenSendFailed());
    producer.setRetryTimesWhenSendAsyncFailed(properties.getRetryTimesWhenSendAsyncFailed());
    producer.setRetryAnotherBrokerWhenNotStoreOK(properties.isRetryNextServer());
    return new RocketMqTemplate(producer);
  }

  static RPCHook aclHook(String accessKey, String secretKey) {
    if (accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
      return null;
    }
    return new AclClientRPCHook(new SessionCredentials(accessKey, secretKey));
  }

  /**
   * Registers a hook that runs before and after every send, for example to copy the tenant id
   * of the current thread into the message properties. Can be called before or after start.
   */
  public RocketMqTemplate registerSendMessageHook(SendMessageHook hook) {
    producer.getDefaultMQProducerImpl().registerSendMessageHook(hook);
    return this;
  }

  public synchronized RocketMqTemplate start() {
    if (started) {
      return this;
    }
    try {
      producer.start();
      started = true;
      log.info("RocketMQ producer started, group={}, nameServer={}", producer.getProducerGroup(),
          producer.getNamesrvAddr());
    } catch (Exception e) {
      throw new RocketMqException("Failed to start RocketMQ producer", e);
    }
    return this;
  }

  public synchronized void destroy() {
    if (started) {
      producer.shutdown();
      started = false;
      log.info("RocketMQ producer stopped, group={}", producer.getProducerGroup());
    }
  }

  public boolean isStarted() {
    return started;
  }

  public DefaultMQProducer getProducer() {
    return producer;
  }

  /**
   * Synchronous send with the default timeout.
   */
  public SendResult send(String destination, Object payload) {
    return syncSend(destination, payload, producer.getSendMsgTimeout());
  }

  /**
   * Synchronous send with a business key.
   */
  public SendResult send(String destination, Object payload, Object keys) {
    return send(destination, wrap(payload).keys(keys));
  }

  public SendResult syncSend(String destination, Object payload) {
    return syncSend(destination, payload, producer.getSendMsgTimeout());
  }

  public SendResult syncSend(String destination, Object payload, long timeout) {
    Message message = createMessage(destination, payload);
    try {
      return producer.send(message, timeout);
    } catch (Exception e) {
      throw sendFailed(destination, e);
    }
  }

  /**
   * Delay message by level. Level n maps to the n-th value of the broker setting
   * {@code messageDelayLevel}, by default {@code 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h}.
   */
  public SendResult syncSend(String destination, Object payload, long timeout, int delayLevel) {
    return syncSend(destination, wrap(payload).delayLevel(delayLevel), timeout);
  }

  /**
   * Delay message with an arbitrary delay in seconds (timer messages).
   */
  public SendResult syncSendDelayTimeSeconds(String destination, Object payload, long delaySeconds) {
    return syncSend(destination, wrap(payload).delaySeconds(delaySeconds));
  }

  /**
   * Delay message with an arbitrary delay in milliseconds (timer messages).
   */
  public SendResult syncSendDelayTimeMills(String destination, Object payload, long delayMillis) {
    return syncSend(destination, wrap(payload).delayMillis(delayMillis));
  }

  /**
   * Message delivered at the given epoch time in milliseconds (timer messages).
   */
  public SendResult syncSendDeliverTimeMills(String destination, Object payload, long deliverTimeMillis) {
    return syncSend(destination, wrap(payload).deliverTimeMillis(deliverTimeMillis));
  }

  /**
   * Ordered send: messages with the same hash key go to the same queue.
   */
  public SendResult syncSendOrderly(String destination, Object payload, String hashKey) {
    return syncSendOrderly(destination, payload, hashKey, producer.getSendMsgTimeout());
  }

  public SendResult syncSendOrderly(String destination, Object payload, String hashKey, long timeout) {
    Message message = createMessage(destination, payload);
    try {
      return producer.send(message, hashSelector, hashKey, timeout);
    } catch (Exception e) {
      throw sendFailed(destination, e);
    }
  }

  public void asyncSend(String destination, Object payload, SendCallback callback) {
    asyncSend(destination, payload, callback, producer.getSendMsgTimeout());
  }

  public void asyncSend(String destination, Object payload, SendCallback callback, long timeout) {
    Message message = createMessage(destination, payload);
    try {
      producer.send(message, callback, timeout);
    } catch (Exception e) {
      throw sendFailed(destination, e);
    }
  }

  public void asyncSendOrderly(String destination, Object payload, String hashKey, SendCallback callback) {
    Message message = createMessage(destination, payload);
    try {
      producer.send(message, hashSelector, hashKey, callback, producer.getSendMsgTimeout());
    } catch (Exception e) {
      throw sendFailed(destination, e);
    }
  }

  /**
   * Fire and forget, no result and no retry. Suitable for logs and metrics.
   */
  public void sendOneWay(String destination, Object payload) {
    Message message = createMessage(destination, payload);
    try {
      producer.sendOneway(message);
    } catch (Exception e) {
      throw sendFailed(destination, e);
    }
  }

  public void sendOneWayOrderly(String destination, Object payload, String hashKey) {
    Message message = createMessage(destination, payload);
    try {
      producer.sendOneway(message, hashSelector, hashKey);
    } catch (Exception e) {
      throw sendFailed(destination, e);
    }
  }

  /**
   * Builds the native message for a destination ({@code topic} or {@code topic:tags}) and payload.
   */
  public Message createMessage(String destination, Object payload) {
    if (destination == null || destination.isEmpty()) {
      throw new RocketMqException("Destination must not be empty");
    }
    String topic = destination;
    String tags = null;
    int colon = destination.indexOf(':');
    if (colon >= 0) {
      topic = destination.substring(0, colon);
      tags = destination.substring(colon + 1);
    }
    RocketMqMessage wrapper = wrap(payload);
    if (wrapper.getTags() != null) {
      tags = wrapper.getTags();
    }
    Message message = new Message(topic, converter.toBody(wrapper.getPayload()));
    if (tags != null && !tags.isEmpty()) {
      message.setTags(tags);
    }
    if (wrapper.getKeys() != null && !wrapper.getKeys().isEmpty()) {
      message.setKeys(wrapper.getKeys());
    }
    for (Map.Entry<String, String> e : wrapper.getProperties().entrySet()) {
      message.putUserProperty(e.getKey(), e.getValue());
    }
    if (wrapper.getDelayLevel() > 0) {
      message.setDelayTimeLevel(wrapper.getDelayLevel());
    }
    if (wrapper.getDelaySeconds() > 0) {
      message.setDelayTimeSec(wrapper.getDelaySeconds());
    }
    if (wrapper.getDelayMillis() > 0) {
      message.setDelayTimeMs(wrapper.getDelayMillis());
    }
    if (wrapper.getDeliverTimeMillis() > 0) {
      message.setDeliverTimeMs(wrapper.getDeliverTimeMillis());
    }
    return message;
  }

  private static RocketMqMessage wrap(Object payload) {
    if (payload instanceof RocketMqMessage) {
      return (RocketMqMessage) payload;
    }
    return RocketMqMessage.of(payload);
  }

  private static RocketMqException sendFailed(String destination, Exception e) {
    if (e instanceof RocketMqException) {
      return (RocketMqException) e;
    }
    return new RocketMqException("Failed to send RocketMQ message to " + destination, e);
  }
}
