package nexus.io.tio.boot.admin.kafaka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface KafkaTopicConsumer {

  public void consume(ConsumerRecord topic);

}
