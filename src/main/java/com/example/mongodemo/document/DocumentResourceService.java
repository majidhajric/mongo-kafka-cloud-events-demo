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
}
