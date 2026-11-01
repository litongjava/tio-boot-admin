package nexus.io.tio.boot.extension.rocketmq;

/**
 * Message listener. The generic type decides how the message body is converted:
 * <ul>
 * <li>{@code String}: body decoded as UTF-8</li>
 * <li>{@code byte[]}: raw body</li>
 * <li>{@code org.apache.rocketmq.common.message.MessageExt}: the original message with all properties</li>
 * <li>any other type: body parsed as JSON</li>
 * </ul>
 * Throwing an exception makes the message be consumed again later.
 *
 * @param <T> payload type
 */
@FunctionalInterface
public interface RocketMqListener<T> {

  void onMessage(T message);
}
