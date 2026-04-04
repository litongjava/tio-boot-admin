package nexus.io.tio.boot.admin.kafaka;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaConsumerRunner implements Runnable {

  private final KafkaConsumer<String, String> consumer;
  private final String topic;
  private final AtomicBoolean running = new AtomicBoolean(true);

  public KafkaConsumerRunner(KafkaConsumer<String, String> consumer, String topic) {
    this.consumer = consumer;
    this.topic = topic;
  }

  @Override
  public void run() {
    consumer.subscribe(Collections.singletonList(topic));
    log.info("Kafka consumer subscribed topic: {}", topic);

    try {
      while (running.get()) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
        for (ConsumerRecord<String, String> record : records) {
          log.info("Received message, topic:{}, partition:{}, offset:{}, key:{}, value:{}", record.topic(),
              record.partition(), record.offset(), record.key(), record.value());
        }
      }
    } catch (Exception e) {
      log.error("Kafka consumer error", e);
    } finally {
      try {
        consumer.close();
      } catch (Exception e) {
        log.error("Kafka consumer close error", e);
      }
    }
  }

  public void shutdown() {
    running.set(false);
  }
}