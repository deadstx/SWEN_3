package org.example.swen3backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.swen3backend.dto.DocumentResponse;
import org.example.swen3backend.service.DocumentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            //Bindet den Teil des Multipart-Requests mit dem Namen "file" an den Parameter
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        DocumentResponse response = documentService.upload(
                file.getOriginalFilename(),
                file.getBytes()
        );

        URI location = URI.create("/api/documents/" + response.id());

        return ResponseEntity.created(location).body(response);
    }
}