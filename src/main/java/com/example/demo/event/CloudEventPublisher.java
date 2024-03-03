/*
 * Copyright (c) 2024. majidhajric@gmail.com
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.example.demo.event;

import com.example.demo.document.DocumentResource;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class CloudEventPublisher {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;

    private final ObjectMapper objectMapper;
    @Value("${spring.kafka.events-topic}")
    private String eventsTopic;

    @EventListener(value = AfterSaveEvent.class)
    @Transactional("kafkaTransactionManager")
    public void onAfterSave(AfterSaveEvent<DocumentResource> event) throws JsonProcessingException {
        DocumentResource documentResource = event.getSource();
        boolean isNew = documentResource.getVersion() == 0;
        String eventType = isNew ? "com.example.document.created" : "com.example.document.updated";
        log.debug("Received event message: {}", documentResource);
        String host = ServletUriComponentsBuilder.fromCurrentRequestUri().build().getHost();
        assert host != null;
        CloudEventBuilder builder = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create(host))
                .withType(eventType)
                .withDataSchema(URI.create(host + "/schemas/document.json"))
                .withSubject(documentResource.getId());

        builder.withData(objectMapper.writeValueAsBytes(documentResource));
        builder.withContextAttribute("principal", "anonymous");

        CloudEvent message = builder.build();

        String subject = message.getSubject();
        assert subject != null;

        kafkaTemplate.send(eventsTopic, subject, message);
    }
}
