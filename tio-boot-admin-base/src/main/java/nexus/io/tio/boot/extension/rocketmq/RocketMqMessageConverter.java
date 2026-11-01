package nexus.io.tio.boot.extension.rocketmq;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import nexus.io.tio.utils.json.JsonUtils;

/**
 * Converts payloads to message bodies and back. Strings are sent as UTF-8, byte arrays as is,
 * everything else as JSON.
 */
public class RocketMqMessageConverter {

  public byte[] toBody(Object payload) {
    if (payload == null) {
      throw new RocketMqException("Message payload must not be null");
    }
    if (payload instanceof byte[]) {
      return (byte[]) payload;
    }
    if (payload instanceof String) {
      return ((String) payload).getBytes(StandardCharsets.UTF_8);
    }
    return JsonUtils.toJsonBytes(payload);
  }

  @SuppressWarnings("unchecked")
  public <T> T fromMessage(MessageExt message, Type type) {
    Class<?> rawType = rawClass(type);
    if (rawType != null) {
      if (MessageExt.class.equals(rawType) || Message.class.equals(rawType)) {
        return (T) message;
      }
      if (byte[].class.equals(rawType)) {
        return (T) message.getBody();
      }
      if (String.class.equals(rawType)) {
        return (T) new String(message.getBody(), StandardCharsets.UTF_8);
      }
      if (Object.class.equals(rawType)) {
        return (T) JsonUtils.parseObject(new String(message.getBody(), StandardCharsets.UTF_8));
      }
    }
    return JsonUtils.parse(message.getBody(), type);
  }

  private static Class<?> rawClass(Type type) {
    if (type instanceof Class) {
      return (Class<?>) type;
    }
    if (type instanceof ParameterizedType) {
      Type raw = ((ParameterizedType) type).getRawType();
      if (raw instanceof Class) {
        return (Class<?>) raw;
      }
    }
    return null;
  }
}
