package at.technikum.swen3backend.DTO;

import java.util.List;

public record UpdateDocumentRequest
        (String name, List<String> tags) {
}
