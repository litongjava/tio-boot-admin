package nexus.io.tio.boot.admin.rocketmq;

/**
 * Message filter type used by a consumer subscription.
 */
public enum RocketMqSelectorType {
  /**
   * Filter by tag expression, for example {@code "*"} or {@code "tagA || tagB"}.
   */
  TAG,

  /**
   * Filter by SQL92 expression over user properties. The broker must enable
   * {@code enablePropertyFilter=true}.
   */
  SQL92
}
