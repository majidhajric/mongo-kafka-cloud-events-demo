package com.example.mongodemo.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentResourceService {

    private final DocumentResourceRepository documentResourceRepository;

    @Transactional
    public DocumentResource save(DocumentResource documentResource) {
        return documentResourceRepository.save(documentResource);
    }

    @Transactional
    public DocumentResource update(String id, DocumentResource documentResource) {
        DocumentResource documentResourceToUpdate = documentResourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No document resource found with id: " + id));
        documentResourceToUpdate.setName(documentResource.getName());
        documentResourceToUpdate.setType(documentResource.getType());
        documentResourceToUpdate.setContent(documentResource.getContent());
        documentResourceToUpdate.setMetadata(documentResource.getMetadata());
        return documentResourceRepository.save(documentResourceToUpdate);
    }
}
