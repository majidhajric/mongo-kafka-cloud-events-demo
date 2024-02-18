package com.example.mongodemo.document;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentResourceRepository extends MongoRepository<DocumentResource, UUID> {
}
