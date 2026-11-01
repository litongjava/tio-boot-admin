package nexus.io.tio.boot.extension.rocketmq;

import nexus.io.tio.utils.environment.EnvUtils;

/**
 * Resolves {@code ${key}} and {@code ${key:default}} placeholders from the tio-boot environment.
 */
public final class RocketMqPlaceholders {

  private RocketMqPlaceholders() {
  }

  public static String resolve(String text) {
    if (text == null || text.indexOf("${") < 0) {
      return text;
    }
    StringBuilder result = new StringBuilder(text.length());
    int index = 0;
    while (index < text.length()) {
      int start = text.indexOf("${", index);
      if (start < 0) {
        result.append(text, index, text.length());
        break;
      }
      int end = text.indexOf('}', start + 2);
      if (end < 0) {
        throw new RocketMqException("Unclosed placeholder in: " + text);
      }
      result.append(text, index, start);
      String expression = text.substring(start + 2, end);
      String key = expression;
      String defaultValue = null;
      int colon = expression.indexOf(':');
      if (colon >= 0) {
        key = expression.substring(0, colon);
        defaultValue = expression.substring(colon + 1);
      }
      String value = EnvUtils.get(key.trim());
      if (value == null) {
        value = defaultValue;
      }
      if (value == null) {
        throw new RocketMqException("Could not resolve placeholder '" + key + "' in: " + text);
      }
      result.append(value);
      index = end + 1;
    }
    return result.toString();
  }
}
