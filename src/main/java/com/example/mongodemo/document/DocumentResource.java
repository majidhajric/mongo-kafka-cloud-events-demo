package com.example.mongodemo.document;

import lombok.Data;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Map;

@Data
@Document(collection = "documents")
public class DocumentResource {

    @MongoId
    @Field("id")
    private String id;
    @Indexed(unique = true)
    private String name;
    @Field("type")
    private String type;
    private byte[] content;
    private Map<String, String> metadata;
    @Version
    private Integer version;

    @PersistenceCreator
    public static DocumentResource create(String name, String type, byte[] content, Map<String, String> metadata) {
        DocumentResource documentResource = new DocumentResource();
        documentResource.setName(name);
        documentResource.setType(type);
        documentResource.setContent(content);
        documentResource.setMetadata(metadata);
        return documentResource;
    }
}
