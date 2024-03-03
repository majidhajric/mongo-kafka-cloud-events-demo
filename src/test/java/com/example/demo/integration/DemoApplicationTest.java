/*
 * Copyright (c) 2024. majidhajric@gmail.com
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.example.demo.integration;

import com.example.demo.DemoApplication;
import com.example.demo.document.DocumentResource;
import com.example.demo.document.DocumentResourceRepository;
import com.example.demo.document.DocumentResourceService;
import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Testcontainers
@Slf4j
@RequiredArgsConstructor
@SpringBootTest(classes = DemoApplication.class)
@ExtendWith(SpringExtension.class)
public class DemoApplicationTest {
    @Container
    public static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"));
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"))
            .withExposedPorts(27017)
            .withCommand("--replSet", "rs0");

    static {
        mongoDBContainer.start();
    }

    @Autowired
    private DocumentResourceRepository documentsRepository;
    @Autowired
    private DocumentResourceService documentsService;
    @MockBean
    private KafkaTemplate<String, CloudEvent> producerTemplate;
    @Autowired
    private MongoTemplate template;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl() + "?uuidRepresentation=STANDARD;");
        registry.add("spring.kafka.bootstrap-servers", () -> kafkaContainer.getBootstrapServers());
    }


    @Test
    void saveTest() {
        DocumentResource document = new DocumentResource();
        document.setType("Exception");
        document.setName("Exception");
        document.setContent("test".getBytes());

        documentsService.save(document);
        List<DocumentResource> resourceList = documentsRepository.findAll();
        assertEquals(1, resourceList.size());
    }

    @Test
    void saveExceptionTest() {
        documentsRepository.deleteAll();
        assertEquals(0, documentsRepository.findAll().size());

        DocumentResource document = new DocumentResource();
        document.setType("Exception");
        document.setName("Exception");
        document.setContent("test".getBytes());

        when(producerTemplate.send(any(), any(), any())).thenThrow(new RuntimeException("Exception"));

        Exception thrown = assertThrows(
                RuntimeException.class,
                () -> documentsService.save(document)
        );

        assertTrue(thrown.getMessage().contains("Exception"));
        assertEquals(0, documentsRepository.findAll().size());
    }
}
