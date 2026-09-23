package org.example.swen3backend;

import org.example.swen3backend.dto.DocumentCollectionResponse;
import org.example.swen3backend.entity.DocumentCollectionEntity;
import org.example.swen3backend.entity.DocumentEntity;
import org.example.swen3backend.mapper.DocumentCollectionMapper;
import org.example.swen3backend.mapper.DocumentMapper;
import org.example.swen3backend.repository.DocumentCollectionRepository;
import org.example.swen3backend.repository.DocumentRepository;
import org.example.swen3backend.service.CollectionNotFoundException;
import org.example.swen3backend.service.DocumentCollectionService;
import org.example.swen3backend.service.DocumentNotFoundException;
import org.example.swen3backend.validation.FieldValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentCollectionServiceTests {
    private DocumentCollectionRepository collections;
    private DocumentRepository documents;
    private DocumentCollectionService service;
    private DocumentCollectionEntity collection;
    private DocumentEntity document;

    @BeforeEach
    void setUp() {
        collections = mock(DocumentCollectionRepository.class);
        documents = mock(DocumentRepository.class);
        service = new DocumentCollectionService(
                collections,
                documents,
                new DocumentCollectionMapper(new DocumentMapper()),
                new FieldValidator()
        );
        collection = new DocumentCollectionEntity("Semester");
        byte[] content = "%PDF-1.7".getBytes(US_ASCII);
        document = spy(new DocumentEntity("test.pdf", content, content.length, Instant.EPOCH));
        // Die Datenbank-ID wird im Test simuliert.
        when(document.getId()).thenReturn(7L);
        when(collections.findById(1L)).thenReturn(Optional.of(collection));
        when(documents.findById(7L)).thenReturn(Optional.of(document));
        when(collections.save(any(DocumentCollectionEntity.class)))
                .thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void createTrimsNameAndReturnsAnEmptyCollection() {
        DocumentCollectionResponse result = service.create(" Semester ");

        assertEquals("Semester", result.name());
        assertTrue(result.documents().isEmpty());
        verify(collections).save(argThat(saved -> saved.getName().equals("Semester")));
    }

    @Test
    void readsReturnCollectionsWithDocumentMetadata() {
        collection.getDocuments().add(document);
        when(collections.findAll()).thenReturn(List.of(collection));

        List<DocumentCollectionResponse> all = service.getAll();
        DocumentCollectionResponse single = service.getById(1L);

        assertEquals(1, all.size());
        assertEquals("Semester", single.name());
        assertEquals(1, single.documents().size());
        assertEquals(7L, single.documents().getFirst().id());
        assertEquals("test.pdf", single.documents().getFirst().name());
        assertEquals(single.documents(), all.getFirst().documents());
    }

    @Test
    void documentCanBelongToTwoCollectionsWithoutDuplicates() {
        DocumentCollectionEntity other = new DocumentCollectionEntity("Other");
        when(collections.findById(2L)).thenReturn(Optional.of(other));

        service.addDocument(1L, 7L);
        service.addDocument(1L, 7L);
        service.addDocument(2L, 7L);

        assertEquals(1, collection.getDocuments().size());
        assertEquals(1, other.getDocuments().size());
        assertTrue(collection.getDocuments().contains(document));
        assertTrue(other.getDocuments().contains(document));
        verify(collections, times(2)).save(collection);
        verify(collections).save(other);
    }

    @Test
    void removingMembershipPreservesTheDocumentAndOtherCollection() {
        DocumentCollectionEntity other = new DocumentCollectionEntity("Other");
        collection.getDocuments().add(document);
        other.getDocuments().add(document);

        service.removeDocument(1L, 7L);

        assertTrue(collection.getDocuments().isEmpty());
        assertTrue(other.getDocuments().contains(document));
        verify(collections).save(collection);
        verify(documents, never()).delete(any());
    }

    @Test
    void deletingCollectionPreservesItsDocuments() {
        collection.getDocuments().add(document);

        service.delete(1L);

        verify(collections).delete(collection);
        verify(documents, never()).delete(any());
    }

    @Test
    void missingCollectionsAndDocumentsAreRejectedWithoutChanges() {
        when(collections.findById(99L)).thenReturn(Optional.empty());
        when(documents.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CollectionNotFoundException.class, () -> service.getById(99L));
        assertThrows(CollectionNotFoundException.class, () -> service.delete(99L));
        assertThrows(CollectionNotFoundException.class, () -> service.addDocument(99L, 7L));
        assertThrows(CollectionNotFoundException.class, () -> service.removeDocument(99L, 7L));
        assertThrows(DocumentNotFoundException.class, () -> service.addDocument(1L, 99L));

        assertTrue(collection.getDocuments().isEmpty());
        verify(collections, never()).save(any());
        verify(collections, never()).delete(any());
    }
}