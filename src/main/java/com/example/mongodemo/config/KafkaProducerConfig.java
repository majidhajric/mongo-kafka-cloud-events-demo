package com.example.mongodemo.config;


import io.cloudevents.CloudEvent;
import io.cloudevents.core.message.Encoding;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.kafka.CloudEventSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.events-topic}")
    private String eventsTopic;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, CloudEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);// Enable safely ordered retries
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        // Config number of retries in case of delivery failure
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 5000);
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 200);
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CloudEventSerializer.class);
        configProps.put(CloudEventSerializer.ENCODING_CONFIG, Encoding.STRUCTURED);
        configProps.put(CloudEventSerializer.EVENT_FORMAT_CONFIG, JsonFormat.CONTENT_TYPE);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, CloudEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public NewTopic eventsTopic() {
        return TopicBuilder
                .name(eventsTopic)
                .build();
    }
}
