package com.example.mongodemo.event;

import com.example.mongodemo.document.DocumentResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class CloudEventPublisher {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;
    @Value("${spring.kafka.events-topic}")
    private String eventsTopic;

    private void sendMessage(final CloudEvent event) {
        log.info("Sending kafka event: {}", event);
        String subject = event.getSubject();
        assert subject != null;
        kafkaTemplate.send(eventsTopic, subject, event);
    }

    @EventListener(value = AfterSaveEvent.class)
    public void onAfterSave(AfterSaveEvent<DocumentResource> event) throws URISyntaxException, JsonProcessingException {
        DocumentResource documentResource = event.getSource();
        log.debug("Received event message: {}", documentResource);
        String host = ServletUriComponentsBuilder.fromCurrentRequestUri().build().getHost();
        assert host != null;
        CloudEventBuilder builder = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create(host))
                .withType("com.example.document.created")
                .withDataSchema(URI.create(host + "/schemas/document.json"))
                .withSubject(documentResource.getId().toString());

        builder.withData(objectMapper.writeValueAsBytes(documentResource));
        builder.withContextAttribute("principal", "anonymous");

        CloudEvent message = builder.build();
        this.sendMessage(message);
    }
}
