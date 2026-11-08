package at.technikum.swen3backend.DTO;

import java.util.List;

public record DocumentCollectionResponse(
        Long id, String name, List<DocumentResponse> documents) {
}
