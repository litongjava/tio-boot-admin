package nexus.io.tio.boot.admin.rocketmq;

import nexus.io.tio.utils.environment.EnvUtils;

/**
 * RocketMQ settings. {@link #fromEnv()} reads them from app.properties / environment variables:
 *
 * <pre>
 * rocketmq.enabled=true
 * rocketmq.name-server=127.0.0.1:9876
 * rocketmq.producer.group=chatGroup
 * rocketmq.producer.send-message-timeout=3000
 * rocketmq.producer.compress-message-body-threshold=4096
 * rocketmq.producer.max-message-size=4194304
 * rocketmq.producer.retry-times-when-send-failed=2
 * rocketmq.producer.retry-times-when-send-async-failed=2
 * rocketmq.producer.retry-next-server=false
 * rocketmq.producer.access-key=
 * rocketmq.producer.secret-key=
 * rocketmq.producer.enable-msg-trace=false
 * rocketmq.producer.customized-trace-topic=RMQ_SYS_TRACE_TOPIC
 * rocketmq.consumer.access-key=
 * rocketmq.consumer.secret-key=
 * rocketmq.consumer.enable-msg-trace=false
 * rocketmq.consumer.customized-trace-topic=RMQ_SYS_TRACE_TOPIC
 * rocketmq.consumer.listeners.&lt;consumerGroup&gt;.&lt;topic&gt;=false
 * </pre>
 */
public class RocketMqProperties {

  public static final String DEFAULT_TRACE_TOPIC = "RMQ_SYS_TRACE_TOPIC";

  private boolean enabled = true;
  private String nameServer;
  private boolean vipChannelEnabled = false;
  private boolean useTls = false;

  private String producerGroup;
  private int sendMessageTimeout = 3000;
  private int compressMessageBodyThreshold = 1024 * 4;
  private int maxMessageSize = 1024 * 1024 * 4;
  private int retryTimesWhenSendFailed = 2;
  private int retryTimesWhenSendAsyncFailed = 2;
  private boolean retryNextServer = false;
  private String producerAccessKey;
  private String producerSecretKey;
  private boolean producerEnableMsgTrace = false;
  private String producerCustomizedTraceTopic = DEFAULT_TRACE_TOPIC;

  private String consumerAccessKey;
  private String consumerSecretKey;
  private boolean consumerEnableMsgTrace = false;
  private String consumerCustomizedTraceTopic = DEFAULT_TRACE_TOPIC;

  public static RocketMqProperties fromEnv() {
    RocketMqProperties p = new RocketMqProperties();
    p.enabled = EnvUtils.getBoolean("rocketmq.enabled", true);
    p.nameServer = EnvUtils.get("rocketmq.name-server");
    p.vipChannelEnabled = EnvUtils.getBoolean("rocketmq.vip-channel-enabled", false);
    p.useTls = EnvUtils.getBoolean("rocketmq.use-tls", false);

    p.producerGroup = EnvUtils.get("rocketmq.producer.group");
    p.sendMessageTimeout = EnvUtils.getInt("rocketmq.producer.send-message-timeout", p.sendMessageTimeout);
    p.compressMessageBodyThreshold = EnvUtils.getInt("rocketmq.producer.compress-message-body-threshold",
        p.compressMessageBodyThreshold);
    p.maxMessageSize = EnvUtils.getInt("rocketmq.producer.max-message-size", p.maxMessageSize);
    p.retryTimesWhenSendFailed = EnvUtils.getInt("rocketmq.producer.retry-times-when-send-failed",
        p.retryTimesWhenSendFailed);
    p.retryTimesWhenSendAsyncFailed = EnvUtils.getInt("rocketmq.producer.retry-times-when-send-async-failed",
        p.retryTimesWhenSendAsyncFailed);
    p.retryNextServer = EnvUtils.getBoolean("rocketmq.producer.retry-next-server", false);
    p.producerAccessKey = EnvUtils.get("rocketmq.producer.access-key");
    p.producerSecretKey = EnvUtils.get("rocketmq.producer.secret-key");
    p.producerEnableMsgTrace = EnvUtils.getBoolean("rocketmq.producer.enable-msg-trace", false);
    p.producerCustomizedTraceTopic = EnvUtils.get("rocketmq.producer.customized-trace-topic", DEFAULT_TRACE_TOPIC);

    p.consumerAccessKey = EnvUtils.get("rocketmq.consumer.access-key");
    p.consumerSecretKey = EnvUtils.get("rocketmq.consumer.secret-key");
    p.consumerEnableMsgTrace = EnvUtils.getBoolean("rocketmq.consumer.enable-msg-trace", false);
    p.consumerCustomizedTraceTopic = EnvUtils.get("rocketmq.consumer.customized-trace-topic", DEFAULT_TRACE_TOPIC);
    return p;
  }

  /**
   * Whether the listener of the given group and topic is switched on by
   * {@code rocketmq.consumer.listeners.<group>.<topic>}. Defaults to true.
   */
  public boolean isListenerEnabled(String consumerGroup, String topic) {
    return EnvUtils.getBoolean("rocketmq.consumer.listeners." + consumerGroup + "." + topic, true);
  }

  /**
   * True when RocketMQ is switched on and a name server is configured.
   */
  public boolean isAvailable() {
    return enabled && nameServer != null && !nameServer.trim().isEmpty();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public RocketMqProperties setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public String getNameServer() {
    return nameServer;
  }

  public RocketMqProperties setNameServer(String nameServer) {
    this.nameServer = nameServer;
    return this;
  }

  public boolean isVipChannelEnabled() {
    return vipChannelEnabled;
  }

  public RocketMqProperties setVipChannelEnabled(boolean vipChannelEnabled) {
    this.vipChannelEnabled = vipChannelEnabled;
    return this;
  }

  public boolean isUseTls() {
    return useTls;
  }

  public RocketMqProperties setUseTls(boolean useTls) {
    this.useTls = useTls;
    return this;
  }

  public String getProducerGroup() {
    return producerGroup;
  }

  public RocketMqProperties setProducerGroup(String producerGroup) {
    this.producerGroup = producerGroup;
    return this;
  }

  public int getSendMessageTimeout() {
    return sendMessageTimeout;
  }

  public RocketMqProperties setSendMessageTimeout(int sendMessageTimeout) {
    this.sendMessageTimeout = sendMessageTimeout;
    return this;
  }

  public int getCompressMessageBodyThreshold() {
    return compressMessageBodyThreshold;
  }

  public RocketMqProperties setCompressMessageBodyThreshold(int compressMessageBodyThreshold) {
    this.compressMessageBodyThreshold = compressMessageBodyThreshold;
    return this;
  }

  public int getMaxMessageSize() {
    return maxMessageSize;
  }

  public RocketMqProperties setMaxMessageSize(int maxMessageSize) {
    this.maxMessageSize = maxMessageSize;
    return this;
  }

  public int getRetryTimesWhenSendFailed() {
    return retryTimesWhenSendFailed;
  }

  public RocketMqProperties setRetryTimesWhenSendFailed(int retryTimesWhenSendFailed) {
    this.retryTimesWhenSendFailed = retryTimesWhenSendFailed;
    return this;
  }

  public int getRetryTimesWhenSendAsyncFailed() {
    return retryTimesWhenSendAsyncFailed;
  }

  public RocketMqProperties setRetryTimesWhenSendAsyncFailed(int retryTimesWhenSendAsyncFailed) {
    this.retryTimesWhenSendAsyncFailed = retryTimesWhenSendAsyncFailed;
    return this;
  }

  public boolean isRetryNextServer() {
    return retryNextServer;
  }

  public RocketMqProperties setRetryNextServer(boolean retryNextServer) {
    this.retryNextServer = retryNextServer;
    return this;
  }

  public String getProducerAccessKey() {
    return producerAccessKey;
  }

  public RocketMqProperties setProducerAccessKey(String producerAccessKey) {
    this.producerAccessKey = producerAccessKey;
    return this;
  }

  public String getProducerSecretKey() {
    return producerSecretKey;
  }

  public RocketMqProperties setProducerSecretKey(String producerSecretKey) {
    this.producerSecretKey = producerSecretKey;
    return this;
  }

  public boolean isProducerEnableMsgTrace() {
    return producerEnableMsgTrace;
  }

  public RocketMqProperties setProducerEnableMsgTrace(boolean producerEnableMsgTrace) {
    this.producerEnableMsgTrace = producerEnableMsgTrace;
    return this;
  }

  public String getProducerCustomizedTraceTopic() {
    return producerCustomizedTraceTopic;
  }

  public RocketMqProperties setProducerCustomizedTraceTopic(String producerCustomizedTraceTopic) {
    this.producerCustomizedTraceTopic = producerCustomizedTraceTopic;
    return this;
  }

  public String getConsumerAccessKey() {
    return consumerAccessKey;
  }

  public RocketMqProperties setConsumerAccessKey(String consumerAccessKey) {
    this.consumerAccessKey = consumerAccessKey;
    return this;
  }

  public String getConsumerSecretKey() {
    return consumerSecretKey;
  }

  public RocketMqProperties setConsumerSecretKey(String consumerSecretKey) {
    this.consumerSecretKey = consumerSecretKey;
    return this;
  }

  public boolean isConsumerEnableMsgTrace() {
    return consumerEnableMsgTrace;
  }

  public RocketMqProperties setConsumerEnableMsgTrace(boolean consumerEnableMsgTrace) {
    this.consumerEnableMsgTrace = consumerEnableMsgTrace;
    return this;
  }

  public String getConsumerCustomizedTraceTopic() {
    return consumerCustomizedTraceTopic;
  }

  public RocketMqProperties setConsumerCustomizedTraceTopic(String consumerCustomizedTraceTopic) {
    this.consumerCustomizedTraceTopic = consumerCustomizedTraceTopic;
    return this;
  }
}
