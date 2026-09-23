package org.example.swen3backend.dto;

import java.util.List;

public record UpdateDocumentRequest(String name, List<String> tags) {
}
