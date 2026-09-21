package com.pranit.github.rag.graph;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class GraphifyMcpClient {

    private final RestClient client;
    private final ObjectMapper objectMapper;

    public GraphifyMcpClient(
            @Value("${app.graphify.mcp-url}")
            String url,
            @Value("${app.graphify.api-key}")
            String apiKey,
            ObjectMapper objectMapper) {

        this.client =
                RestClient.builder()
                        .baseUrl(url)
                        .defaultHeader(
                                "Authorization",
                                "Bearer " + apiKey)
                        .build();

        this.objectMapper =
                objectMapper;
    }

    public JsonNode callTool(
            String tool,
            Map<String, Object> arguments) {

        Map<String, Object> request =
                Map.of(
                        "jsonrpc",
                        "2.0",
                        "id",
                        1,
                        "method",
                        "tools/call",
                        "params",
                        Map.of(
                                "name",
                                tool,
                                "arguments",
                                arguments));

        String response =
                client.post()
                        .body(request)
                        .retrieve()
                        .body(String.class);

        try {

            return objectMapper.readTree(
                    response);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Invalid Graphify MCP response",
                    e);
        }
    }
}