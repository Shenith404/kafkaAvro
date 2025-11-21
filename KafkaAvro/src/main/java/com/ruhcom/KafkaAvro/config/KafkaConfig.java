package com.ruhcom.KafkaAvro.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConfig.class);

    @Value("${app.kafka.topic}")
    private String orderTopic;
    @Value("${app.kafka.dlq-topic}")
    private String dlqTopic;
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.properties.schema-registry-url}")
    private String schemaRegistryUrl;
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value("${spring.kafka.producer.client-id}")
    private String clientId;
    @Value("${app.kafka.replication-factor:3}")
    private short replicationFactor;
    @Value("${app.kafka.partitions:5}")
    private int partitions;
    @Value("${app.kafka.retry-attempts:5}")
    private long retryAttempts;
    @Value("${app.kafka.retry-backoff-ms:3000}")
    private long retryBackoffMs;
    @Value("${app.kafka.consumer-concurrency:3}")
    private int consumerConcurrency;

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
    public NewTopic submissionsTopic() {
        logger.info("Creating Kafka topic: {} with {} partitions and replication factor {}", orderTopic, partitions, replicationFactor);
        return new NewTopic(orderTopic, partitions, replicationFactor);
    }

    @Bean
    @ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
    public NewTopic dlqTopic() {
        logger.info("Creating Kafka DLQ topic: {} with replication factor {}", dlqTopic, replicationFactor);
        return new NewTopic(dlqTopic, 1, replicationFactor);
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        return new DefaultKafkaProducerFactory<>(props);
    }


    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Let Spring manage commits for retry logic
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put("specific.avro.reader", true);
        return new DefaultKafkaConsumerFactory<>(props);
    }


    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> template) {
        // Let Kafka distribute DLQ messages across partitions of the DLQ topic
        logger.info("DeadLetterPublishingRecoverer configured to publish to DLQ topic: {}", dlqTopic);
        return new DeadLetterPublishingRecoverer(template, (r, e) -> {
            logger.error("Sending message to DLQ topic: {} for record: {}, Exception: {}", dlqTopic, r.key(), e.getMessage());
            return new org.apache.kafka.common.TopicPartition(dlqTopic, 0); // Use partition 0 for DLQ
        });
    }


    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        // Configurable retry with backoff
        FixedBackOff backOff = new FixedBackOff(retryBackoffMs, retryAttempts);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        // Add retry listener for logging
        handler.setRetryListeners((record, ex, deliveryAttempt) -> {
            logger.warn("=== RETRY ATTEMPT {} ===", deliveryAttempt);
            logger.warn("Retrying message with key: {} on topic: {}", record.key(), record.topic());
            logger.warn("Exception: {}", ex.getMessage());
            logger.warn("Next retry in {}ms", retryBackoffMs);
        });
        // Add non-retryable exceptions that should go directly to DLQ
        handler.addNotRetryableExceptions(IllegalArgumentException.class, NullPointerException.class, ClassCastException.class, org.apache.kafka.common.errors.SerializationException.class);
        return handler;
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(consumerConcurrency); // Configurable concurrency
        factory.setCommonErrorHandler(errorHandler);
        // Set container properties for proper error handling
        factory.getContainerProperties().setAckMode(org.springframework.kafka.listener.ContainerProperties.AckMode.RECORD);
        logger.info("Kafka listener factory configured with concurrency: {} and RECORD acknowledgment mode", consumerConcurrency);
        return factory;
    }


}
