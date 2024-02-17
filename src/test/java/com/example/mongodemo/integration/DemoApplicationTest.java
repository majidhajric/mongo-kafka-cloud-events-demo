package com.example.mongodemo.integration;

import com.example.mongodemo.DemoApplication;
import com.example.mongodemo.document.DocumentResource;
import com.example.mongodemo.document.DocumentResourceRepository;
import com.example.mongodemo.document.DocumentResourceService;
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
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl() + "&retryWrites=true&ssl=true&maxIdleTimeMS=5000");
        registry.add("spring.kafka.bootstrap-servers", () -> kafkaContainer.getBootstrapServers());
    }


    @Test
    void saveTest() {
        DocumentResource document = new DocumentResource();
        document.setId("1");
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
        document.setId("1");
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
