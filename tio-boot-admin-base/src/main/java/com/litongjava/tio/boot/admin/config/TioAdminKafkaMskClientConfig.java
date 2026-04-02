package com.litongjava.tio.boot.admin.config;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.litongjava.hook.HookCan;
import com.litongjava.tio.boot.admin.kafaka.KafkaConsumerRunner;
import com.litongjava.tio.boot.admin.kafaka.KafkaProducerUtils;
import com.litongjava.tio.boot.admin.utils.AwsProfileUtils;
import com.litongjava.tio.utils.environment.EnvUtils;
import com.litongjava.tio.utils.hutool.StrUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TioAdminKafkaMskClientConfig {

  private KafkaConsumerRunner consumerRunner;
  private Thread consumerThread;
  private KafkaProducer<String, String> producer;

  public void config() {
    String bootstrapServers = EnvUtils.get("aws.msk.bootstrap-servers");
    if (StrUtil.isBlank(bootstrapServers)) {
      log.info("aws.msk.bootstrap-servers is blank, skip MSK client initialization");
      return;
    }

    boolean enableProducer = EnvUtils.getBoolean("aws.msk.enable-producer", true);
    boolean enableConsumer = EnvUtils.getBoolean("aws.msk.enable-consumer", true);

    if (!enableProducer && !enableConsumer) {
      log.info("Both producer and consumer are disabled, skip MSK client initialization");
      return;
    }

    String producerTopicName = EnvUtils.get("aws.msk.producer.topic-name");
    String consumerTopicName = EnvUtils.get("aws.msk.consumer.topic-name");
    String groupId = EnvUtils.get("aws.msk.group-id");

    String acks = EnvUtils.get("aws.msk.acks", "all");
    int retries = EnvUtils.getInt("aws.msk.retries", 3);
    String autoOffsetReset = EnvUtils.get("aws.msk.auto-offset-reset", "earliest");
    boolean enableAutoCommit = EnvUtils.getBoolean("aws.msk.enable-auto-commit", true);

    String jaasConfig = AwsProfileUtils.jaasConfig();

    if (enableProducer) {
      initProducer(bootstrapServers, producerTopicName, acks, retries, jaasConfig);
    } else {
      log.info("Kafka producer disabled, config: aws.msk.enable-producer=false");
    }

    if (enableConsumer) {
      initConsumer(bootstrapServers, consumerTopicName, groupId, autoOffsetReset, enableAutoCommit, jaasConfig);
    } else {
      log.info("Kafka consumer disabled, config: aws.msk.enable-consumer=false");
    }

    HookCan.me().addDestroyMethod(() -> {
      try {
        log.info("Shutting down Kafka resources");

        if (consumerRunner != null) {
          consumerRunner.shutdown();
        }

        if (consumerThread != null) {
          consumerThread.interrupt();
        }

        if (producer != null) {
          producer.close();
        }

        log.info("Kafka resources shutdown completed");
      } catch (Exception e) {
        log.error("Shutdown Kafka resources failed", e);
      }
    });
  }

  private void initProducer(String bootstrapServers, String producerTopicName, String acks, int retries, String jaasConfig) {
    Properties producerProps = new Properties();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    producerProps.put(ProducerConfig.ACKS_CONFIG, acks);
    producerProps.put(ProducerConfig.RETRIES_CONFIG, retries);

    producerProps.put("security.protocol", "SASL_SSL");
    producerProps.put("sasl.mechanism", "AWS_MSK_IAM");
    producerProps.put("sasl.jaas.config", jaasConfig);
    producerProps.put("sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.IAMClientCallbackHandler");

    producer = new KafkaProducer<>(producerProps);

    if (StrUtil.isNotBlank(producerTopicName)) {
      KafkaProducerUtils.init(producer, producerTopicName);
      log.info("Kafka producer initialized, default topicName:{}", producerTopicName);
    } else {
      log.info("Kafka producer initialized without default topic, dynamic topic mode");
    }
  }

  private void initConsumer(String bootstrapServers, String consumerTopicName, String groupId, String autoOffsetReset,
      boolean enableAutoCommit, String jaasConfig) {

    if (StrUtil.isBlank(consumerTopicName)) {
      log.info("aws.msk.consumer.topic-name is blank, skip Kafka consumer initialization");
      return;
    }

    if (StrUtil.isBlank(groupId)) {
      log.info("aws.msk.group-id is blank, skip Kafka consumer initialization");
      return;
    }

    Properties consumerProps = new Properties();
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
    consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);

    consumerProps.put("security.protocol", "SASL_SSL");
    consumerProps.put("sasl.mechanism", "AWS_MSK_IAM");
    consumerProps.put("sasl.jaas.config", jaasConfig);
    consumerProps.put("sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.IAMClientCallbackHandler");

    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);

    consumerRunner = new KafkaConsumerRunner(consumer, consumerTopicName);
    consumerThread = new Thread(consumerRunner, "msk-consumer-thread");
    consumerThread.start();

    log.info("Kafka consumer started, groupId:{}, topicName:{}", groupId, consumerTopicName);
  }
}