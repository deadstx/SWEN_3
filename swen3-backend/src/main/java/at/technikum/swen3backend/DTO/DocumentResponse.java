package at.technikum.swen3backend.DTO;

import java.time.Instant;
import java.util.List;

public record DocumentResponse(
        Long id,
        String name,
        long fileSize,
        Instant uploadedAt,
        List<String> tags
) {
}