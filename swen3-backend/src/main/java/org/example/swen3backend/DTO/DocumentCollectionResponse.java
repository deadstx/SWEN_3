package org.example.swen3backend.dto;

import java.util.List;

public record DocumentCollectionResponse(
        Long id, String name, List<DocumentResponse> documents) {
}
