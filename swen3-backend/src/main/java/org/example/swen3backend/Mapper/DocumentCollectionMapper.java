package org.example.swen3backend.mapper;

import lombok.RequiredArgsConstructor;
import org.example.swen3backend.dto.DocumentCollectionResponse;
import org.example.swen3backend.dto.DocumentResponse;
import org.example.swen3backend.entity.DocumentCollectionEntity;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentCollectionMapper extends EntityMapper<DocumentCollectionEntity, DocumentCollectionResponse> {

    private final DocumentMapper documentMapper;

    @Override
    public DocumentCollectionResponse toDto(DocumentCollectionEntity collection) {
        List<DocumentResponse> documents = collection.getDocuments().stream()
                .map(documentMapper::toDto)
                .toList();

        return new DocumentCollectionResponse(collection.getId(), collection.getName(), documents);
    }
}
