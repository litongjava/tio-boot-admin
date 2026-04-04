package nexus.io.tio.boot.admin.kafaka;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaConsumerRunner implements Runnable {

  private final KafkaConsumer<String, String> consumer;
  private final KafkaTopicConsumer topicConsumer;
  private String topic;
  private final AtomicBoolean running = new AtomicBoolean(true);

  public KafkaConsumerRunner(KafkaConsumer<String, String> consumer, String topic, KafkaTopicConsumer topicConsumer) {
    this.consumer = consumer;
    this.topic = topic;
    this.topicConsumer = topicConsumer;
  }

  public KafkaConsumerRunner(KafkaConsumer<String, String> consumer, KafkaTopicConsumer topicConsumer) {
    this.consumer = consumer;
    this.topicConsumer = topicConsumer;
  }

  @Override
  public void run() {
    if (topic != null) {
      List<String> singletonList = Collections.singletonList(topic);
      consumer.subscribe(singletonList);
      log.info("Kafka consumer subscribed topic: {}", topic);
    }

    try {
      while (running.get()) {
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
        for (ConsumerRecord<String, String> record : records) {
          if (topicConsumer != null) {
            topicConsumer.consume(record);
          } else {
            log.info("Received message, topic:{}, partition:{}, offset:{}, key:{}, value:{}", record.topic(),
                record.partition(), record.offset(), record.key(), record.value());
          }
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