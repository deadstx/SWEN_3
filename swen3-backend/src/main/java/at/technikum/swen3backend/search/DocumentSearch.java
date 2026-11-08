package at.technikum.swen3backend.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class DocumentSearch {
    private final RestClient client;

    public DocumentSearch(@Value("${elasticsearch.url}") String url) {
        this.client = RestClient.builder().baseUrl(url).build();
    }

    @SuppressWarnings("unchecked")
    public List<Long> search(String query) {
        Map<String, Object> body = Map.of(
                "size", 50,
                "_source", false,
                "query", Map.of("multi_match", Map.of(
                        "query", query,
                        "fields", List.of("text", "fileName"),
                        "fuzziness", "AUTO"))); // fuzzy für tippfehler
        try {
            Map<String, Object> response = client.post()
                    .uri("/documents/_search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            List<Map<String, Object>> hits =
                    (List<Map<String, Object>>) ((Map<String, Object>) response.get("hits")).get("hits");
            return hits.stream().map(hit -> Long.parseLong((String) hit.get("_id"))).toList();
        } catch (HttpClientErrorException.NotFound exception) {
            return List.of(); // Index existiert nd = es wurde noch nix indexiert.
        }
    }

    public void delete(long id) {
        try {
            client.delete().uri("/documents/_doc/{id}", id).retrieve().toBodilessEntity();
        } catch (HttpClientErrorException.NotFound exception) {
            // Nicht indexiert: nix zu löschen.
        }
    }
}