package nexus.io.tio.boot.admin.rocketmq;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.junit.Test;

import nexus.io.tio.utils.environment.EnvUtils;

public class RocketMqUnitTest {

  public static class UserDto {
    private Long uid;
    private String name;

    public UserDto() {
    }

    public UserDto(Long uid, String name) {
      this.uid = uid;
      this.name = name;
    }

    public Long getUid() {
      return uid;
    }

    public void setUid(Long uid) {
      this.uid = uid;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  @RocketMqMessageListener(topic = "push_${test.node-id}", consumerGroup = "push_group_${test.node-id:9}",
      messageModel = MessageModel.BROADCASTING, maxReconsumeTimes = 3)
  public static class UserListener implements RocketMqListener<UserDto> {
    @Override
    public void onMessage(UserDto message) {
    }
  }

  public static abstract class BaseListener<T> implements RocketMqListener<T> {
  }

  public static class ListListener implements RocketMqListener<List<UserDto>> {
    @Override
    public void onMessage(List<UserDto> message) {
    }
  }

  @Test
  public void placeholdersAreResolvedFromEnv() {
    EnvUtils.set("test.node-id", "7");
    assertEquals("push_7", RocketMqPlaceholders.resolve("push_${test.node-id}"));
    assertEquals("a_x_b", RocketMqPlaceholders.resolve("a_${missing.key:x}_b"));
    assertEquals("plain", RocketMqPlaceholders.resolve("plain"));
  }

  @Test(expected = RocketMqException.class)
  public void unresolvedPlaceholderFails() {
    RocketMqPlaceholders.resolve("${definitely.not.set.key}");
  }

  @Test
  public void annotationIsReadIntoOptions() {
    EnvUtils.set("test.node-id", "7");
    RocketMqConsumerOptions options = RocketMqConsumerOptions
        .from(UserListener.class.getAnnotation(RocketMqMessageListener.class));
    assertEquals("push_7", options.getTopic());
    assertEquals("push_group_7", options.getConsumerGroup());
    assertEquals(MessageModel.BROADCASTING, options.getMessageModel());
    assertEquals(3, options.getMaxReconsumeTimes());
    assertEquals("*", options.getSelectorExpression());
    assertNull(options.getEnableMsgTrace());
  }

  @Test
  public void payloadTypeIsResolved() {
    assertEquals(UserDto.class, RocketMqListenerContainer.resolvePayloadType(UserListener.class));
    Type type = RocketMqListenerContainer.resolvePayloadType(ListListener.class);
    assertTrue(type instanceof ParameterizedType);
    assertEquals(List.class, ((ParameterizedType) type).getRawType());
  }

  @Test(expected = RocketMqException.class)
  public void lambdaPayloadTypeNeedsExplicitType() {
    RocketMqListener<String> lambda = message -> {
    };
    RocketMqListenerContainer.resolvePayloadType(lambda.getClass());
  }

  @Test
  public void converterRoundTrip() {
    RocketMqMessageConverter converter = new RocketMqMessageConverter();
    MessageExt ext = new MessageExt();
    ext.setBody(converter.toBody(new UserDto(1L, "tom")));
    UserDto dto = converter.fromMessage(ext, UserDto.class);
    assertEquals(Long.valueOf(1L), dto.getUid());
    assertEquals("tom", dto.getName());

    ext.setBody(converter.toBody("hello"));
    assertEquals("hello", converter.fromMessage(ext, String.class));
    assertSame(ext, converter.fromMessage(ext, MessageExt.class));
    byte[] raw = new byte[] { 1, 2, 3 };
    assertArrayEquals(raw, converter.toBody(raw));
  }

  @Test
  public void createMessageParsesDestinationAndOptions() {
    RocketMqTemplate template = new RocketMqTemplate(new DefaultMQProducer("unit_test_group"));
    Message m = template.createMessage("order_topic:created",
        RocketMqMessage.of("body").keys(123L).property("tenantId", 1).delayLevel(2));
    assertEquals("order_topic", m.getTopic());
    assertEquals("created", m.getTags());
    assertEquals("123", m.getKeys());
    assertEquals("1", m.getUserProperty("tenantId"));
    assertEquals(2, m.getDelayTimeLevel());
    assertEquals("body", new String(m.getBody(), StandardCharsets.UTF_8));

    Message timer = template.createMessage("t", RocketMqMessage.of("x").delaySeconds(5).tags("tagB"));
    assertEquals("tagB", timer.getTags());
    assertEquals(5L, timer.getDelayTimeSec());
  }

  @Test
  public void disabledWithoutNameServer() {
    RocketMqProperties p = new RocketMqProperties();
    assertTrue(!p.isAvailable());
    p.setNameServer("127.0.0.1:9876");
    assertTrue(p.isAvailable());
    p.setEnabled(false);
    assertTrue(!p.isAvailable());
  }
}
