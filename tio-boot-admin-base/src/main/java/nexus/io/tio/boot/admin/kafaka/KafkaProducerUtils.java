package nexus.io.tio.boot.admin.kafaka;

import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaProducerUtils {

  private static KafkaProducer<String, String> producer;
  private static String topic;

  public static void init(KafkaProducer<String, String> kafkaProducer, String topicName) {
    producer = kafkaProducer;
    topic = topicName;
  }

  public static KafkaProducer<String, String> getProducer() {
    return producer;
  }

  public static void send(String message) {
    send(topic, message);
  }

  public static void send(String topicName, String message) {
    try {
      ProducerRecord<String, String> record = new ProducerRecord<>(topicName, message);
      Future<RecordMetadata> future = producer.send(record);
      RecordMetadata metadata = future.get();
      log.info("Kafka message sent, topic:{}, partition:{}, offset:{}", metadata.topic(), metadata.partition(),
          metadata.offset());
    } catch (Exception e) {
      log.error("Kafka send message failed", e);
    }
  }
}