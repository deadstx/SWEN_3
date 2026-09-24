package org.example.swen3backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.swen3backend.dto.CreateCollectionRequest;
import org.example.swen3backend.dto.DocumentCollectionResponse;
import org.example.swen3backend.service.DocumentCollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class DocumentCollectionController {

    private final DocumentCollectionService collectionService;

    @PostMapping
    public ResponseEntity<DocumentCollectionResponse> create(@RequestBody CreateCollectionRequest request) {
        DocumentCollectionResponse response = collectionService.create(request.name());
        return ResponseEntity.created(URI.create("/api/collections/" + response.id())).body(response);
    }

    @GetMapping
    public List<DocumentCollectionResponse> getAll() {
        return collectionService.getAll();
    }

    @GetMapping("/{id}")
    public DocumentCollectionResponse getById(@PathVariable("id") long id) {
        return collectionService.getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) {
        collectionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/documents/{documentId}")
    public ResponseEntity<Void> addDocument(@PathVariable("id") long id,
                                          @PathVariable("documentId") long documentId) {
        collectionService.addDocument(id, documentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/documents/{documentId}")
    public ResponseEntity<Void> removeDocument(@PathVariable("id") long id,
                                               @PathVariable("documentId") long documentId) {
        collectionService.removeDocument(id, documentId);
        return ResponseEntity.noContent().build();
    }
}
