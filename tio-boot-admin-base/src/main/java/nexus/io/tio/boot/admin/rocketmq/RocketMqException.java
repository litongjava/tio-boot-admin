package nexus.io.tio.boot.admin.rocketmq;

/**
 * Unchecked exception thrown by the RocketMQ integration.
 */
public class RocketMqException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public RocketMqException(String message) {
    super(message);
  }

  public RocketMqException(String message, Throwable cause) {
    super(message, cause);
  }
}
