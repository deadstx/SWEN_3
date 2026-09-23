package org.example.swen3backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.swen3backend.dto.DocumentResponse;
import org.example.swen3backend.dto.UpdateDocumentRequest;
import org.example.swen3backend.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        DocumentResponse response = documentService.upload(
                file.getOriginalFilename(),
                file.getBytes()
        );

        URI location = URI.create("/api/documents/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<DocumentResponse> getAll() {
        return documentService.getAll();
    }

    @GetMapping("/{id}")
    public DocumentResponse getById(@PathVariable("id") long id) {
        return documentService.getById(id);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public DocumentResponse update(
            @PathVariable("id") long id,
            @RequestBody UpdateDocumentRequest request
    ) {
        return documentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}