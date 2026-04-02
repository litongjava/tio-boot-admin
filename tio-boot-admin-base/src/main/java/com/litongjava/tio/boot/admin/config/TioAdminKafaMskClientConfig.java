package com.litongjava.tio.boot.admin.config;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.litongjava.annotation.AConfiguration;
import com.litongjava.annotation.Initialization;
import com.litongjava.hook.HookCan;
import com.litongjava.tio.boot.admin.kafaka.KafkaConsumerRunner;
import com.litongjava.tio.boot.admin.kafaka.KafkaProducerUtils;
import com.litongjava.tio.utils.environment.EnvUtils;

import lombok.extern.slf4j.Slf4j;

@AConfiguration
@Slf4j
public class TioAdminKafaMskClientConfig {

  private KafkaConsumerRunner consumerRunner;
  private Thread consumerThread;
  private KafkaProducer<String, String> producer;

  @Initialization
  public void init() {
    String bootstrapServers = EnvUtils.get("aws.msk.bootstrap-servers");
    String topicName = EnvUtils.get("aws.msk.topic-name");
    String groupId = EnvUtils.get("aws.msk.group-id");

    String acks = EnvUtils.get("aws.msk.acks", "all");
    int retries = EnvUtils.getInt("aws.msk.retries", 3);
    String autoOffsetReset = EnvUtils.get("aws.msk.auto-offset-reset", "earliest");
    boolean enableAutoCommit = EnvUtils.getBoolean("aws.msk.enable-auto-commit", true);

    // Producer properties
    Properties producerProps = new Properties();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProps.put(ProducerConfig.ACKS_CONFIG, acks);
    producerProps.put(ProducerConfig.RETRIES_CONFIG, retries);

    // MSK IAM auth
    producerProps.put("security.protocol", "SASL_SSL");
    producerProps.put("sasl.mechanism", "AWS_MSK_IAM");
    producerProps.put("sasl.jaas.config", "software.amazon.msk.auth.iam.IAMLoginModule required;");
    producerProps.put("sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.IAMClientCallbackHandler");

    producer = new KafkaProducer<>(producerProps);
    KafkaProducerUtils.init(producer, topicName);
    log.info("Kafka producer initialized");

    // Consumer properties
    Properties consumerProps = new Properties();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
    consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);

    // MSK IAM auth
    consumerProps.put("security.protocol", "SASL_SSL");
    consumerProps.put("sasl.mechanism", "AWS_MSK_IAM");
    consumerProps.put("sasl.jaas.config", "software.amazon.msk.auth.iam.IAMLoginModule required;");
    consumerProps.put("sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.IAMClientCallbackHandler");

    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);

    consumerRunner = new KafkaConsumerRunner(consumer, topicName);
    consumerThread = new Thread(consumerRunner, "msk-consumer-thread");
    consumerThread.start();
    log.info("Kafka consumer started");

    HookCan.me().addDestroyMethod(() -> {
      try {
        log.info("Shutting down Kafka resources");
        if (consumerRunner != null) {
          consumerRunner.shutdown();
        }
        if (producer != null) {
          producer.close();
        }
      } catch (Exception e) {
        log.error("Shutdown Kafka resources failed", e);
      }
    });
  }
}