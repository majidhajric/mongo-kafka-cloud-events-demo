package com.example.mongodemo.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1/documents")
public class DocumentResourceController {

    private final DocumentResourceService service;

    @PostMapping
    @ResponseStatus(CREATED)
    public DocumentResource save(@RequestBody DocumentResource documentResource) {
        return service.save(documentResource);
    }
}
