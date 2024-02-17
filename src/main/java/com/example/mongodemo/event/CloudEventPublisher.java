package com.example.mongodemo.event;

import com.example.mongodemo.document.DocumentResource;
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

import java.net.URISyntaxException;

@Slf4j
@RequiredArgsConstructor
@Component
public class CloudEventPublisher {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;
    @Value("${spring.kafka.events-topic}")
    private String eventsTopic;

    private void sendMessage(CloudEvent event) {
        log.info("Sending kafka event: {}", event);
        kafkaTemplate.send(eventsTopic, event.getId(), event);
    }

    @EventListener(value = AfterSaveEvent.class)
    public void onAfterSave(AfterSaveEvent<DocumentResource> event) throws URISyntaxException {
        DocumentResource documentResource = event.getSource();
        log.debug("Received event message: {}", documentResource);
        CloudEvent message = CloudEventBuilder.v1()
                .withId(documentResource.getId())
                .withType("document-created")
                .withSource(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri())
                .build();
        this.sendMessage(message);
    }
}
