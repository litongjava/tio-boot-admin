package nexus.io.tio.boot.extension.rocketmq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.rocketmq.client.hook.ConsumeMessageContext;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import nexus.io.tio.utils.environment.EnvUtils;

/**
 * Runs against a real broker:
 * mvn test -Dtest=RocketMqIntegrationTest -Drocketmq.it.name-server=127.0.0.1:9876
 */
public class RocketMqIntegrationTest {

  private static final String RUN = String.valueOf(System.currentTimeMillis());
  private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

  private static final BlockingQueue<Received> typed = new ArrayBlockingQueue<>(16);
  private static final BlockingQueue<MessageExt> raw = new ArrayBlockingQueue<>(16);
  private static final BlockingQueue<String> tagged = new ArrayBlockingQueue<>(16);
  private static final BlockingQueue<Long> delayed = new ArrayBlockingQueue<>(16);
  private static final List<String> broadcastA = new CopyOnWriteArrayList<>();
  private static final List<String> broadcastB = new CopyOnWriteArrayList<>();
  private static final List<Integer> ordered = Collections.synchronizedList(new ArrayList<Integer>());
  private static final AtomicInteger retryAttempts = new AtomicInteger();
  private static final CountDownLatch retryDone = new CountDownLatch(1);

  static class Received {
    final RocketMqUnitTest.UserDto dto;
    final String tenant;

    Received(RocketMqUnitTest.UserDto dto, String tenant) {
      this.dto = dto;
      this.tenant = tenant;
    }
  }

  static class TimedPayload {
    private long sentAt;

    public TimedPayload() {
    }

    public TimedPayload(long sentAt) {
      this.sentAt = sentAt;
    }

    public long getSentAt() {
      return sentAt;
    }

    public void setSentAt(long sentAt) {
      this.sentAt = sentAt;
    }
  }

  @RocketMqMessageListener(topic = "it_user_${it.run}", consumerGroup = "it_user_group_${it.run}")
  public static class UserListener implements RocketMqListener<RocketMqUnitTest.UserDto> {
    @Override
    public void onMessage(RocketMqUnitTest.UserDto message) {
      typed.add(new Received(message, TENANT.get()));
    }
  }

  @RocketMqMessageListener(topic = "it_raw_${it.run}", consumerGroup = "it_raw_group_${it.run}")
  public static class RawListener implements RocketMqListener<MessageExt> {
    @Override
    public void onMessage(MessageExt message) {
      raw.add(message);
    }
  }

  @RocketMqMessageListener(topic = "it_tag_${it.run}", consumerGroup = "it_tag_group_${it.run}", selectorExpression = "tagA")
  public static class TagListener implements RocketMqListener<String> {
    @Override
    public void onMessage(String message) {
      tagged.add(message);
    }
  }

  @RocketMqMessageListener(topic = "it_delay_${it.run}", consumerGroup = "it_delay_group_${it.run}")
  public static class DelayListener implements RocketMqListener<TimedPayload> {
    @Override
    public void onMessage(TimedPayload message) {
      delayed.add(System.currentTimeMillis() - message.getSentAt());
    }
  }

  @RocketMqMessageListener(topic = "it_retry_${it.run}", consumerGroup = "it_retry_group_${it.run}", maxReconsumeTimes = 3, delayLevelWhenNextConsume = 1)
  public static class RetryListener implements RocketMqListener<String> {
    @Override
    public void onMessage(String message) {
      if (retryAttempts.incrementAndGet() == 1) {
        throw new IllegalStateException("first attempt fails on purpose");
      }
      retryDone.countDown();
    }
  }

  public static class TenantSendHook implements SendMessageHook {
    @Override
    public String hookName() {
      return "TenantSendHook";
    }

    @Override
    public void sendMessageBefore(SendMessageContext context) {
      String tenant = TENANT.get();
      if (tenant != null) {
        context.getMessage().putUserProperty("tenantId", tenant);
      }
    }

    @Override
    public void sendMessageAfter(SendMessageContext context) {
    }
  }

  public static class TenantConsumeHook implements ConsumeMessageHook {
    @Override
    public String hookName() {
      return "TenantConsumeHook";
    }

    @Override
    public void consumeMessageBefore(ConsumeMessageContext context) {
      TENANT.set(context.getMsgList().get(0).getUserProperty("tenantId"));
    }

    @Override
    public void consumeMessageAfter(ConsumeMessageContext context) {
      TENANT.remove();
    }
  }

  private static String nameServer;

  @BeforeClass
  public static void setUp() {
    nameServer = System.getProperty("rocketmq.it.name-server");
    Assume.assumeTrue("set -Drocketmq.it.name-server to run", nameServer != null && !nameServer.isEmpty());
    // Pick up newly created topics quickly in tests.
    System.setProperty("rocketmq.client.rebalance.waitInterval", "1000");

    EnvUtils.set("it.run", RUN);
    EnvUtils.set("rocketmq.consumer.listeners.it_off_group_" + RUN + ".it_off_" + RUN, "false");
    RocketMqProperties properties = new RocketMqProperties().setNameServer(nameServer)
        .setProducerGroup("it_producer_" + RUN);
    RocketMq.init(properties);
    RocketMq.addSendMessageHook(new TenantSendHook());
    RocketMq.addConsumeMessageHook(new TenantConsumeHook());

    RocketMq.addListener(new UserListener());
    RocketMq.addListener(new RawListener());
    RocketMq.addListener(new TagListener());
    RocketMq.addListener(new DelayListener());
    RocketMq.addListener(new RetryListener());

    RocketMqConsumerOptions broadcast = RocketMqConsumerOptions.of("it_bc_" + RUN, "it_bc_group_" + RUN)
        .messageModel(MessageModel.BROADCASTING);
    RocketMq.addListener(broadcast, (RocketMqListener<String>) broadcastA::add, String.class);
    RocketMq.addListener(broadcast, (RocketMqListener<String>) broadcastB::add, String.class);

    RocketMqConsumerOptions orderly = RocketMqConsumerOptions.of("it_order_" + RUN, "it_order_group_" + RUN)
        .consumeMode(RocketMqConsumeMode.ORDERLY);
    RocketMq.addListener(orderly, (RocketMqListener<Integer>) ordered::add, Integer.class);

    for (RocketMqListenerContainer container : RocketMq.getContainers()) {
      container.getConsumer().setPollNameServerInterval(1000);
    }
    RocketMq.start();
  }

  @AfterClass
  public static void tearDown() {
    if (nameServer != null) {
      RocketMq.close();
    }
  }

  @Test
  public void typedPayloadAndTenantHooks() throws Exception {
    TENANT.set("42");
    try {
      SendResult result = RocketMq.send("it_user_" + RUN, new RocketMqUnitTest.UserDto(1001L, "alice"));
      assertEquals(SendStatus.SEND_OK, result.getSendStatus());
    } finally {
      TENANT.remove();
    }
    Received received = typed.poll(60, TimeUnit.SECONDS);
    assertNotNull("typed message not received", received);
    assertEquals(Long.valueOf(1001L), received.dto.getUid());
    assertEquals("alice", received.dto.getName());
    assertEquals("42", received.tenant);
  }

  @Test
  public void keysTagsAndPropertiesOnRawMessage() throws Exception {
    RocketMq.getTemplate().send("it_raw_" + RUN + ":created",
        RocketMqMessage.of("payload").keys("order-1").property("traceId", "t-1"));
    MessageExt message = raw.poll(60, TimeUnit.SECONDS);
    assertNotNull("raw message not received", message);
    assertEquals("order-1", message.getKeys());
    assertEquals("created", message.getTags());
    assertEquals("t-1", message.getUserProperty("traceId"));
  }

  @Test
  public void tagFilter() throws Exception {
    RocketMq.getTemplate().send("it_tag_" + RUN + ":tagB", "skip-me");
    RocketMq.getTemplate().send("it_tag_" + RUN + ":tagA", "take-me");
    assertEquals("take-me", tagged.poll(60, TimeUnit.SECONDS));
    assertNull(tagged.poll(3, TimeUnit.SECONDS));
  }

  @Test
  public void delayLevelAndTimerMessages() throws Exception {
    // Make sure the consumer is attached before measuring delays.
    RocketMq.getTemplate().send("it_delay_" + RUN, new TimedPayload(System.currentTimeMillis()));
    assertNotNull(delayed.poll(60, TimeUnit.SECONDS));

    // Level 2 is 3s with the broker setting "1s 3s 5s ..." (5s with the default levels).
    RocketMq.getTemplate().syncSend("it_delay_" + RUN, new TimedPayload(System.currentTimeMillis()), 3000, 2);
    Long levelDelay = delayed.poll(30, TimeUnit.SECONDS);
    assertNotNull(levelDelay);
    assertTrue("delay level message arrived too early: " + levelDelay, levelDelay >= 2500);

    RocketMq.getTemplate().syncSendDelayTimeSeconds("it_delay_" + RUN, new TimedPayload(System.currentTimeMillis()), 2);
    Long timerDelay = delayed.poll(30, TimeUnit.SECONDS);
    assertNotNull(timerDelay);
    assertTrue("timer message arrived too early: " + timerDelay, timerDelay >= 1500);
  }

  @Test
  public void broadcastingReachesEveryInstance() throws Exception {
    // A broadcasting consumer starts from the latest offset, so wait until both instances are attached.
    long deadline = System.currentTimeMillis() + 60000;
    while (System.currentTimeMillis() < deadline && (broadcastA.isEmpty() || broadcastB.isEmpty())) {
      RocketMq.getTemplate().send("it_bc_" + RUN, "probe");
      Thread.sleep(1000);
    }
    RocketMq.getTemplate().send("it_bc_" + RUN, "hello-all");
    deadline = System.currentTimeMillis() + 30000;
    while (System.currentTimeMillis() < deadline
        && (!broadcastA.contains("hello-all") || !broadcastB.contains("hello-all"))) {
      Thread.sleep(200);
    }
    assertTrue(broadcastA.contains("hello-all"));
    assertTrue(broadcastB.contains("hello-all"));
  }

  @Test
  public void orderlyConsumption() throws Exception {
    for (int i = 0; i < 20; i++) {
      RocketMq.getTemplate().syncSendOrderly("it_order_" + RUN, i, "room-1");
    }
    long deadline = System.currentTimeMillis() + 60000;
    while (System.currentTimeMillis() < deadline && ordered.size() < 20) {
      Thread.sleep(200);
    }
    assertEquals(20, ordered.size());
    for (int i = 0; i < 20; i++) {
      assertEquals(Integer.valueOf(i), ordered.get(i));
    }
  }

  @Test
  public void failedMessageIsRetried() throws Exception {
    RocketMq.getTemplate().send("it_retry_" + RUN, "retry-me");
    assertTrue("message was not redelivered", retryDone.await(90, TimeUnit.SECONDS));
    assertEquals(2, retryAttempts.get());
  }

  @Test
  public void listenerCanBeSwitchedOffByProperty() {
    RocketMqListenerContainer container = RocketMq.addListener(
        RocketMqConsumerOptions.of("it_off_" + RUN, "it_off_group_" + RUN), (RocketMqListener<String>) m -> {
        }, String.class);
    assertNull(container);
  }

  @Test
  public void asyncAndOneWaySend() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    RocketMq.getTemplate().asyncSend("it_async_" + RUN + ":async", "async-body",
        new SendCallback() {
          @Override
          public void onSuccess(SendResult sendResult) {
            latch.countDown();
          }

          @Override
          public void onException(Throwable e) {
          }
        });
    assertTrue(latch.await(30, TimeUnit.SECONDS));
    RocketMq.getTemplate().sendOneWay("it_async_" + RUN + ":oneway", "oneway-body");
  }
}
