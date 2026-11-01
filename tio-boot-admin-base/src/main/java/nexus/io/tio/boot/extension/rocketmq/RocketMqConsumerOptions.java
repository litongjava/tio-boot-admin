package nexus.io.tio.boot.extension.rocketmq;

import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;

/**
 * Subscription settings of one listener. Built from {@link RocketMqMessageListener} or in code:
 *
 * <pre>
 * RocketMqConsumerOptions options = RocketMqConsumerOptions.of("order_topic", "order_group")
 *     .messageModel(MessageModel.BROADCASTING).maxReconsumeTimes(3);
 * </pre>
 */
public class RocketMqConsumerOptions {

  private String topic;
  private String consumerGroup;
  private RocketMqSelectorType selectorType = RocketMqSelectorType.TAG;
  private String selectorExpression = "*";
  private RocketMqConsumeMode consumeMode = RocketMqConsumeMode.CONCURRENTLY;
  private MessageModel messageModel = MessageModel.CLUSTERING;
  private int maxReconsumeTimes = -1;
  private int consumeThreadNumber = 20;
  private long consumeTimeout = 15L;
  private int delayLevelWhenNextConsume = 0;
  private long suspendCurrentQueueTimeMillis = 1000L;
  private String nameServer;
  private String accessKey;
  private String secretKey;
  private Boolean enableMsgTrace;
  private String customizedTraceTopic;

  public static RocketMqConsumerOptions of(String topic, String consumerGroup) {
    RocketMqConsumerOptions options = new RocketMqConsumerOptions();
    options.topic = topic;
    options.consumerGroup = consumerGroup;
    return options;
  }

  /**
   * Reads the annotation and resolves {@code ${...}} placeholders.
   */
  public static RocketMqConsumerOptions from(RocketMqMessageListener annotation) {
    RocketMqConsumerOptions options = of(RocketMqPlaceholders.resolve(annotation.topic()),
        RocketMqPlaceholders.resolve(annotation.consumerGroup()));
    options.selectorType = annotation.selectorType();
    options.selectorExpression = RocketMqPlaceholders.resolve(annotation.selectorExpression());
    options.consumeMode = annotation.consumeMode();
    options.messageModel = annotation.messageModel();
    options.maxReconsumeTimes = annotation.maxReconsumeTimes();
    options.consumeThreadNumber = annotation.consumeThreadNumber();
    options.consumeTimeout = annotation.consumeTimeout();
    options.delayLevelWhenNextConsume = annotation.delayLevelWhenNextConsume();
    options.suspendCurrentQueueTimeMillis = annotation.suspendCurrentQueueTimeMillis();
    options.nameServer = emptyToNull(RocketMqPlaceholders.resolve(annotation.nameServer()));
    options.accessKey = emptyToNull(RocketMqPlaceholders.resolve(annotation.accessKey()));
    options.secretKey = emptyToNull(RocketMqPlaceholders.resolve(annotation.secretKey()));
    String trace = RocketMqPlaceholders.resolve(annotation.enableMsgTrace());
    if (trace != null && !trace.isEmpty()) {
      options.enableMsgTrace = Boolean.valueOf(trace);
    }
    options.customizedTraceTopic = emptyToNull(RocketMqPlaceholders.resolve(annotation.customizedTraceTopic()));
    return options;
  }

  private static String emptyToNull(String value) {
    if (value == null || value.isEmpty()) {
      return null;
    }
    return value;
  }

  public RocketMqConsumerOptions selector(RocketMqSelectorType selectorType, String selectorExpression) {
    this.selectorType = selectorType;
    this.selectorExpression = selectorExpression;
    return this;
  }

  public RocketMqConsumerOptions consumeMode(RocketMqConsumeMode consumeMode) {
    this.consumeMode = consumeMode;
    return this;
  }

  public RocketMqConsumerOptions messageModel(MessageModel messageModel) {
    this.messageModel = messageModel;
    return this;
  }

  public RocketMqConsumerOptions maxReconsumeTimes(int maxReconsumeTimes) {
    this.maxReconsumeTimes = maxReconsumeTimes;
    return this;
  }

  public RocketMqConsumerOptions consumeThreadNumber(int consumeThreadNumber) {
    this.consumeThreadNumber = consumeThreadNumber;
    return this;
  }

  public RocketMqConsumerOptions consumeTimeout(long consumeTimeoutMinutes) {
    this.consumeTimeout = consumeTimeoutMinutes;
    return this;
  }

  public RocketMqConsumerOptions delayLevelWhenNextConsume(int delayLevelWhenNextConsume) {
    this.delayLevelWhenNextConsume = delayLevelWhenNextConsume;
    return this;
  }

  public RocketMqConsumerOptions suspendCurrentQueueTimeMillis(long suspendCurrentQueueTimeMillis) {
    this.suspendCurrentQueueTimeMillis = suspendCurrentQueueTimeMillis;
    return this;
  }

  public RocketMqConsumerOptions nameServer(String nameServer) {
    this.nameServer = nameServer;
    return this;
  }

  public RocketMqConsumerOptions acl(String accessKey, String secretKey) {
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    return this;
  }

  public RocketMqConsumerOptions msgTrace(boolean enableMsgTrace, String customizedTraceTopic) {
    this.enableMsgTrace = enableMsgTrace;
    this.customizedTraceTopic = customizedTraceTopic;
    return this;
  }

  public String getTopic() {
    return topic;
  }

  public String getConsumerGroup() {
    return consumerGroup;
  }

  public RocketMqSelectorType getSelectorType() {
    return selectorType;
  }

  public String getSelectorExpression() {
    return selectorExpression;
  }

  public RocketMqConsumeMode getConsumeMode() {
    return consumeMode;
  }

  public MessageModel getMessageModel() {
    return messageModel;
  }

  public int getMaxReconsumeTimes() {
    return maxReconsumeTimes;
  }

  public int getConsumeThreadNumber() {
    return consumeThreadNumber;
  }

  public long getConsumeTimeout() {
    return consumeTimeout;
  }

  public int getDelayLevelWhenNextConsume() {
    return delayLevelWhenNextConsume;
  }

  public long getSuspendCurrentQueueTimeMillis() {
    return suspendCurrentQueueTimeMillis;
  }

  public String getNameServer() {
    return nameServer;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public Boolean getEnableMsgTrace() {
    return enableMsgTrace;
  }

  public String getCustomizedTraceTopic() {
    return customizedTraceTopic;
  }
}
