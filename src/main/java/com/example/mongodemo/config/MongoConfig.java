package com.example.mongodemo.config;

import com.example.mongodemo.document.DocumentResource;
import com.example.mongodemo.document.DocumentResourceRepository;
import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.UUID;

@RequiredArgsConstructor
@EnableMongoRepositories(basePackageClasses = DocumentResourceRepository.class)
@Configuration
public class MongoConfig {

    private final MongoProperties properties;

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(MongoClients.create(properties.getUri()), properties.getDatabase());
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory factory) {
        return new MongoTemplate(factory);
    }

    @Bean
    public BeforeConvertCallback<DocumentResource> beforeConvertCallback() {
        return (documentResource, collectionName) -> {
            if (documentResource.getId() == null) {
                documentResource.setId(UUID.randomUUID());
            }
            return documentResource;
        };
    }
}
