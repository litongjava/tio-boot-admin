package nexus.io.tio.boot.admin.rocketmq;

/**
 * How a push consumer dispatches messages to the listener.
 */
public enum RocketMqConsumeMode {
  /**
   * Messages are consumed concurrently by the consumer thread pool.
   */
  CONCURRENTLY,

  /**
   * Messages of the same message queue are consumed one by one in order.
   */
  ORDERLY
}
