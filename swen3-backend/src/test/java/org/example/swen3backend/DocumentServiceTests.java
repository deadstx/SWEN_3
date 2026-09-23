package org.example.swen3backend;

import org.example.swen3backend.dto.DocumentResponse;
import org.example.swen3backend.dto.UpdateDocumentRequest;
import org.example.swen3backend.entity.DocumentEntity;
import org.example.swen3backend.mapper.DocumentMapper;
import org.example.swen3backend.repository.DocumentRepository;
import org.example.swen3backend.service.DocumentNotFoundException;
import org.example.swen3backend.service.DocumentService;
import org.example.swen3backend.service.InvalidDocumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocumentServiceTests {

    private static final byte[] PDF = "%PDF-1.7\ntest".getBytes(US_ASCII);
    private static final Instant UPLOADED_AT = Instant.parse("2026-09-23T08:00:00Z");

    private DocumentRepository repository;
    private DocumentService service;
    private DocumentEntity document;

    @BeforeEach
    void setUp() {
        repository = mock(DocumentRepository.class);
        service = new DocumentService(repository, new DocumentMapper());
        document = new DocumentEntity("test.pdf", PDF, PDF.length, UPLOADED_AT);
        document.getTags().add("existing");

        when(repository.findById(1L)).thenReturn(Optional.of(document));
        // Das Mock-Repository gibt beim Speichern dasselbe Objekt zurück.
        when(repository.save(any(DocumentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void uploadPdfReturnsMetadata() {
        DocumentResponse result = service.upload("C:\\uploads\\test.pdf", PDF);

        assertEquals("test.pdf", result.name());
        assertEquals(PDF.length, result.fileSize());
        assertNotNull(result.uploadedAt());
        assertEquals(List.of(), result.tags());
        verify(repository).save(argThat(saved -> Arrays.equals(PDF, saved.getContent())));
    }

    @Test
    void uploadRejectsEmptyFile() {
        assertThrows(InvalidDocumentException.class,
                () -> service.upload("empty.pdf", new byte[0]));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadRejectsNonPdfFile() {
        assertThrows(InvalidDocumentException.class,
                () -> service.upload("fake.pdf", "plain text".getBytes(US_ASCII)));

        verify(repository, never()).save(any());
    }

    @Test
    void uploadRejectsMissingOrBlankFilename() {
        assertThrows(InvalidDocumentException.class, () -> service.upload(null, PDF));
        assertThrows(InvalidDocumentException.class, () -> service.upload(" ", PDF));

        verify(repository, never()).save(any());
    }

    @Test
    void getAllReturnsDocuments() {
        when(repository.findAll()).thenReturn(List.of(document));

        List<DocumentResponse> result = service.getAll();

        assertEquals(1, result.size());
        assertEquals("test.pdf", result.getFirst().name());
        assertEquals(List.of("existing"), result.getFirst().tags());
    }

    @Test
    void getByIdReturnsDocument() {
        DocumentResponse result = service.getById(1L);

        assertEquals("test.pdf", result.name());
        assertEquals(PDF.length, result.fileSize());
        assertEquals(UPLOADED_AT, result.uploadedAt());
    }

    @Test
    void missingDocumentIsRejected() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DocumentNotFoundException.class, () -> service.getById(99L));
        assertThrows(DocumentNotFoundException.class,
                () -> service.update(99L, new UpdateDocumentRequest("new.pdf", null)));
        assertThrows(DocumentNotFoundException.class, () -> service.delete(99L));
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }

    @Test
    void nonPositiveIdIsRejected() {
        assertThrows(InvalidDocumentException.class, () -> service.getById(0L));
        assertThrows(InvalidDocumentException.class, () -> service.delete(-1L));

        verify(repository, never()).delete(any());
    }

    @Test
    void updateNamePreservesOtherData() {
        DocumentResponse result = service.update(1L, new UpdateDocumentRequest(" new.pdf ", null));

        assertEquals("new.pdf", result.name());
        assertEquals(List.of("existing"), result.tags());
        assertEquals(PDF.length, document.getFileSize());
        assertEquals(UPLOADED_AT, document.getUploadedAt());
        assertArrayEquals(PDF, document.getContent());
        verify(repository).save(document);
    }

    @Test
    void updateTagsPreservesName() {
        DocumentResponse result = service.update(1L,
                new UpdateDocumentRequest(null, List.of(" invoice ")));

        assertEquals("test.pdf", result.name());
        assertEquals(List.of("invoice"), result.tags());
        verify(repository).save(document);
    }

    @Test
    void updateCanClearTags() {
        DocumentResponse result = service.update(1L, new UpdateDocumentRequest(null, List.of()));

        assertEquals(List.of(), result.tags());
        assertEquals(List.of(), document.getTags());
        verify(repository).save(document);
    }

    @Test
    void updateRejectsEmptyRequest() {
        assertThrows(InvalidDocumentException.class,
                () -> service.update(1L, new UpdateDocumentRequest(null, null)));

        verify(repository, never()).save(any());
    }

    @Test
    void updateRejectsInvalidName() {
        assertThrows(InvalidDocumentException.class,
                () -> service.update(1L, new UpdateDocumentRequest(" ", null)));
        assertThrows(InvalidDocumentException.class,
                () -> service.update(1L, new UpdateDocumentRequest("x".repeat(256), null)));

        assertEquals("test.pdf", document.getName());
        verify(repository, never()).save(any());
    }

    @Test
    void invalidTagsDoNotChangeTheDocument() {
        assertThrows(InvalidDocumentException.class,
                () -> service.update(1L, new UpdateDocumentRequest("new.pdf", List.of(" "))));
        assertThrows(InvalidDocumentException.class,
                () -> service.update(1L, new UpdateDocumentRequest("new.pdf", Collections.singletonList(null))));
        assertThrows(InvalidDocumentException.class,
                () -> service.update(1L, new UpdateDocumentRequest("new.pdf", List.of("x".repeat(256)))));

        assertEquals("test.pdf", document.getName());
        assertEquals(List.of("existing"), document.getTags());
        verify(repository, never()).save(any());
    }

    @Test
    void deleteRemovesDocument() {
        service.delete(1L);

        verify(repository).delete(document);
    }
}

