package nexus.io.tio.boot.admin.rocketmq;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Outgoing message with optional keys, tags, user properties and delay settings.
 *
 * <pre>
 * RocketMqMessage message = RocketMqMessage.of(dto).keys(orderId).property("tenantId", "1");
 * RocketMq.getTemplate().send("order_topic:created", message);
 * </pre>
 */
public class RocketMqMessage {

  private final Object payload;
  private String keys;
  private String tags;
  private final Map<String, String> properties = new LinkedHashMap<>();
  private int delayLevel;
  private long delaySeconds;
  private long delayMillis;
  private long deliverTimeMillis;

  private RocketMqMessage(Object payload) {
    this.payload = payload;
  }

  public static RocketMqMessage of(Object payload) {
    return new RocketMqMessage(payload);
  }

  /**
   * Business keys, used for querying a message in the dashboard. Multiple keys are separated by a space.
   */
  public RocketMqMessage keys(Object keys) {
    this.keys = keys == null ? null : keys.toString();
    return this;
  }

  /**
   * Tags; overrides the tag part of a {@code topic:tag} destination.
   */
  public RocketMqMessage tags(String tags) {
    this.tags = tags;
    return this;
  }

  public RocketMqMessage property(String name, Object value) {
    if (value != null) {
      properties.put(name, value.toString());
    }
    return this;
  }

  /**
   * Delay level of the broker {@code messageDelayLevel} setting, starting at 1.
   */
  public RocketMqMessage delayLevel(int delayLevel) {
    this.delayLevel = delayLevel;
    return this;
  }

  /**
   * Arbitrary delay in seconds (timer messages, broker 5.x).
   */
  public RocketMqMessage delaySeconds(long delaySeconds) {
    this.delaySeconds = delaySeconds;
    return this;
  }

  /**
   * Arbitrary delay in milliseconds (timer messages, broker 5.x).
   */
  public RocketMqMessage delayMillis(long delayMillis) {
    this.delayMillis = delayMillis;
    return this;
  }

  /**
   * Absolute delivery time as epoch milliseconds (timer messages, broker 5.x).
   */
  public RocketMqMessage deliverTimeMillis(long deliverTimeMillis) {
    this.deliverTimeMillis = deliverTimeMillis;
    return this;
  }

  public Object getPayload() {
    return payload;
  }

  public String getKeys() {
    return keys;
  }

  public String getTags() {
    return tags;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public int getDelayLevel() {
    return delayLevel;
  }

  public long getDelaySeconds() {
    return delaySeconds;
  }

  public long getDelayMillis() {
    return delayMillis;
  }

  public long getDeliverTimeMillis() {
    return deliverTimeMillis;
  }
}
