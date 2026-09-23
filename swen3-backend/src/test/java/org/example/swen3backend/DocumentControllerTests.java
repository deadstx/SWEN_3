package org.example.swen3backend;

import org.example.swen3backend.controller.DocumentController;
import org.example.swen3backend.dto.DocumentResponse;
import org.example.swen3backend.dto.UpdateDocumentRequest;
import org.example.swen3backend.service.DocumentNotFoundException;
import org.example.swen3backend.service.DocumentService;
import org.example.swen3backend.service.InvalidDocumentException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTests {

    private static final byte[] PDF_CONTENT = "%PDF-1.7".getBytes(StandardCharsets.US_ASCII);
    private static final DocumentResponse DOCUMENT = new DocumentResponse(
            7L, "test.pdf", PDF_CONTENT.length,
            Instant.parse("2026-09-23T10:00:00Z"), List.of());

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DocumentService service;

    @Test
    void uploadReturns201WithLocationAndMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", PDF_CONTENT);
        when(service.upload("test.pdf", PDF_CONTENT)).thenReturn(DOCUMENT);

        mvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/documents/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("test.pdf"))
                .andExpect(jsonPath("$.fileSize").value(PDF_CONTENT.length))
                .andExpect(jsonPath("$.uploadedAt").value("2026-09-23T10:00:00Z"))
                .andExpect(jsonPath("$.tags").isEmpty())
                .andExpect(jsonPath("$.content").doesNotExist());

        verify(service).upload("test.pdf", PDF_CONTENT);
    }

    @Test
    void uploadWithoutFileReturns400() throws Exception {
        mvc.perform(multipart("/api/documents"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void invalidUploadReturns400() throws Exception {
        byte[] emptyContent = new byte[0];
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", emptyContent);
        when(service.upload("empty.pdf", emptyContent))
                .thenThrow(new InvalidDocumentException("Die Datei darf nicht leer sein."));

        mvc.perform(multipart("/api/documents").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Die Datei darf nicht leer sein."));
    }

    @Test
    void getAllReturnsDocuments() throws Exception {
        when(service.getAll()).thenReturn(List.of(DOCUMENT));

        mvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].name").value("test.pdf"));
    }

    @Test
    void getAllReturnsAnEmptyListWhenNoDocumentsExist() throws Exception {
        when(service.getAll()).thenReturn(List.of());

        mvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getByIdReturnsDocument() throws Exception {
        when(service.getById(7L)).thenReturn(DOCUMENT);

        mvc.perform(get("/api/documents/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("test.pdf"));
    }

    @Test
    void getByIdReturns404ForMissingDocument() throws Exception {
        when(service.getById(99L)).thenThrow(new DocumentNotFoundException(99L));

        mvc.perform(get("/api/documents/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchPassesJsonToServiceAndReturnsUpdatedDocument() throws Exception {
        UpdateDocumentRequest request = new UpdateDocumentRequest("renamed.pdf", List.of("study"));
        DocumentResponse updated = new DocumentResponse(
                7L, "renamed.pdf", DOCUMENT.fileSize(), DOCUMENT.uploadedAt(), List.of("study"));
        when(service.update(7L, request)).thenReturn(updated);

        mvc.perform(patch("/api/documents/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"renamed.pdf\",\"tags\":[\"study\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("renamed.pdf"))
                .andExpect(jsonPath("$.tags[0]").value("study"));
        verify(service).update(7L, request);
    }

    @Test
    void deleteReturns204WithoutBody() throws Exception {
        mvc.perform(delete("/api/documents/7"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).delete(7L);
    }

    @Test
    void deleteReturns404ForMissingDocument() throws Exception {
        doThrow(new DocumentNotFoundException(99L)).when(service).delete(99L);

        mvc.perform(delete("/api/documents/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void malformedIdReturns400() throws Exception {
        mvc.perform(get("/api/documents/abc"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}