package org.example.swen3backend.mapper;

import org.example.swen3backend.dto.DocumentResponse;
import org.example.swen3backend.entity.DocumentEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentMapper extends EntityMapper<DocumentEntity, DocumentResponse> {

    @Override
    public DocumentResponse toDto(DocumentEntity document) {
        return new DocumentResponse(
                document.getId(),
                document.getName(),
                document.getFileSize(),
                document.getUploadedAt(),
                List.copyOf(document.getTags())
        );
    }
}