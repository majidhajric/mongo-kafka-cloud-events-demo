/*
 * Copyright (c) 2024. majidhajric@gmail.com
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.example.demo.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public List<DocumentResource> findAll() {
        return documentResourceRepository.findAll();
    }
}
