package dev.chanler.knownote.research.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.chanler.knownote.config.TavilyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Tavily 搜索 API 客户端
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TavilyClient {
    private final TavilyProperties tavilyProperties;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.parse("application/json");

    public TavilyResponse search(String query, int maxResults, String topic, boolean includeRawContent) {
        try {
            TavilyRequest request = new TavilyRequest(
                query, maxResults, topic, includeRawContent
            );

            String json = objectMapper.writeValueAsString(request);
            RequestBody body = RequestBody.create(json, JSON);

            Request httpRequest = new Request.Builder()
                .url(tavilyProperties.getBaseUrl() + "/search")
                .addHeader("Authorization", "Bearer " + tavilyProperties.getApiKey())
                .post(body)
                .build();

            log.debug("Tavily search: query='{}', maxResults={}, topic='{}'", query, maxResults, topic);

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log.error("Tavily API failed: code={}", response.code());
                    return new TavilyResponse(List.of());
                }
                return objectMapper.readValue(response.body().string(), TavilyResponse.class);
            }
        } catch (IOException e) {
            log.error("Tavily search failed for: {}", query, e);
            return new TavilyResponse(List.of());
        }
    }

    public record TavilyRequest(
        String query,
        @JsonProperty("max_results") int maxResults,
        String topic,
        @JsonProperty("include_raw_content") boolean includeRawContent
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TavilyResponse(List<SearchResult> results) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchResult(
        String url,
        String title,
        String content,
        @JsonProperty("raw_content") String rawContent,
        Double score
    ) {}
}
