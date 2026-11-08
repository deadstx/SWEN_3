package at.technikum.swen3backend.Mapper;

import lombok.RequiredArgsConstructor;
import at.technikum.swen3backend.DTO.DocumentCollectionResponse;
import at.technikum.swen3backend.DTO.DocumentResponse;
import at.technikum.swen3backend.Entity.DocumentCollectionEntity;
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
