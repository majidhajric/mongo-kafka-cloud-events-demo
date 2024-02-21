package com.example.mongodemo.document;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentResourceRepository extends MongoRepository<DocumentResource, String> {
}
