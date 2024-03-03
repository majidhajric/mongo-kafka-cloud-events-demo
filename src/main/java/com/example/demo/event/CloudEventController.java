package com.example.demo.event;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/events")
public class CloudEventController {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    @Value("${spring.kafka.events-topic}")
    private String eventsTopic;

    @PostMapping
    @Transactional("kafkaTransactionManager")
    public void sendMessage(final CloudEvent event) {
        log.info("Sending kafka event: {}", event);
        String subject = event.getSubject();
        assert subject != null;
        CloudEvent cloudEvent = new CloudEventBuilder(event)
                .withSubject(subject)
                .build();
        kafkaTemplate.send(eventsTopic, subject, cloudEvent);
    }

    @GetMapping(path = "/{id}")
    public CloudEvent findOne(@PathVariable String id) {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        assert kafkaStreams != null;
        ReadOnlyKeyValueStore<String, CloudEvent> store = kafkaStreams.store(StoreQueryParameters.fromNameAndType("events-store", QueryableStoreTypes.keyValueStore()));
        return store.get(id);
    }

}
