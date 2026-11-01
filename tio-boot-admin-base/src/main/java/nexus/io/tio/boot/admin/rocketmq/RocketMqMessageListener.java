package nexus.io.tio.boot.admin.rocketmq;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;

/**
 * Declares the subscription of a {@link RocketMqListener} implementation.
 * <p>
 * {@code topic}, {@code consumerGroup}, {@code selectorExpression}, {@code nameServer},
 * {@code accessKey} and {@code secretKey} support placeholders such as
 * {@code ${app.node-id}} or {@code ${app.node-id:1}}, resolved from the tio-boot environment.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RocketMqMessageListener {

  /**
   * Consumer group, required.
   */
  String consumerGroup();

  /**
   * Topic, required.
   */
  String topic();

  RocketMqSelectorType selectorType() default RocketMqSelectorType.TAG;

  String selectorExpression() default "*";

  RocketMqConsumeMode consumeMode() default RocketMqConsumeMode.CONCURRENTLY;

  /**
   * CLUSTERING: every message is handled by one instance of the group.
   * BROADCASTING: every instance of the group receives every message.
   */
  MessageModel messageModel() default MessageModel.CLUSTERING;

  /**
   * Max retry count before the message goes to the dead letter queue. -1 uses the client default (16).
   */
  int maxReconsumeTimes() default -1;

  int consumeThreadNumber() default 20;

  /**
   * Consume timeout in minutes.
   */
  long consumeTimeout() default 15L;

  /**
   * Delay level used for the next retry in concurrent mode. 0 lets the broker decide.
   */
  int delayLevelWhenNextConsume() default 0;

  /**
   * Suspend time in ORDERLY mode after a failure, in milliseconds.
   */
  long suspendCurrentQueueTimeMillis() default 1000L;

  /**
   * Overrides {@code rocketmq.name-server} for this listener when not empty.
   */
  String nameServer() default "";

  /**
   * Overrides {@code rocketmq.consumer.access-key} when not empty.
   */
  String accessKey() default "";

  /**
   * Overrides {@code rocketmq.consumer.secret-key} when not empty.
   */
  String secretKey() default "";

  /**
   * "true" / "false" / "" (empty uses {@code rocketmq.consumer.enable-msg-trace}).
   */
  String enableMsgTrace() default "";

  /**
   * Overrides {@code rocketmq.consumer.customized-trace-topic} when not empty.
   */
  String customizedTraceTopic() default "";
}
