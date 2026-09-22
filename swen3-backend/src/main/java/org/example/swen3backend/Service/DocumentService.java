package org.example.swen3backend.service;

import lombok.RequiredArgsConstructor;
import org.example.swen3backend.dto.DocumentResponse;
import org.example.swen3backend.entity.DocumentEntity;
import org.example.swen3backend.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    public DocumentResponse upload(String originalFilename, byte[] content) {
        String name = normalizeFilename(originalFilename);
        validateContent(content);

        DocumentEntity document = new DocumentEntity(name, content);
        //saved enthält danach auch die generierte id und uploadedAt
        DocumentEntity saved = documentRepository.save(document);

        //TODO: Mapper
        return new DocumentResponse(
                saved.getId(),
                saved.getName(),
                saved.getFileSize(),
                saved.getUploadedAt(),
                //List.of() für tags
                List.of()
        );
    }

    private String normalizeFilename(String originalFilename) {
        if (originalFilename == null) {
            throw new InvalidDocumentException("Ein Dateiname ist erforderlich.");
        }

        String normalized = originalFilename.replace('\\', '/');
        String name = normalized
                .substring(normalized.lastIndexOf('/') + 1)
                //Wenn kein / vorkommt, ist lastIndexOf('/') = -1, also substring(0) = ganzer String
                .strip();

        if (name.isBlank() || name.length() > 255) {
            throw new InvalidDocumentException(
                    "Der Dateiname muss zwischen 1 und 255 Zeichen enthalten."
            );
        }

        return name;
    }

    private void validateContent(byte[] content) {
        if (content == null || content.length == 0) {
            throw new InvalidDocumentException("Die Datei darf nicht leer sein.");
        }

        if (content.length < 5 ||
                !"%PDF-".equals(new String(
                        content, 0, 5, StandardCharsets.US_ASCII))) {
            throw new InvalidDocumentException("Es sind nur PDF-Dateien erlaubt.");
        }
    }
}