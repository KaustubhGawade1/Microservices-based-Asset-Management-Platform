package com.microservice.mcp_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class McpToolService {

    private final RestClient assetRestClient;

    public McpToolService(RestClient assetRestClient) {
        this.assetRestClient = assetRestClient;
    }

    public List<Map<String, Object>> tools() {
        return List.of(
                tool("search_assets", "Search assets by filename or metadata.",
                        Map.of("query", Map.of("type", "string", "description", "Search text"))),
                tool("list_public_assets", "List assets marked as public.", Map.of())
        );
    }

    public Map<String, Object> call(String name, Map<String, Object> arguments) {
        String result;

        if ("search_assets".equals(name)) {
            String query = String.valueOf(arguments.getOrDefault("query", ""));
            Object assets = assetRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/search").queryParam("query", query).build())
                    .retrieve()
                    .body(Object.class);
            result = assets == null ? "[]" : assets.toString();
        } else if ("list_public_assets".equals(name)) {
            Object assets = assetRestClient.get()
                    .uri("/api/public/asset")
                    .retrieve()
                    .body(Object.class);
            result = assets == null ? "[]" : assets.toString();
        } else {
            throw new UnknownToolException(name);
        }

        return Map.of(
                "content", List.of(Map.of("type", "text", "text", result)),
                "isError", false
        );
    }

    private Map<String, Object> tool(String name, String description, Map<String, Object> properties) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);

        Map<String, Object> tool = new LinkedHashMap<>();
        tool.put("name", name);
        tool.put("description", description);
        tool.put("inputSchema", schema);
        return tool;
    }

    public static class UnknownToolException extends RuntimeException {
        public UnknownToolException(String name) {
            super("Unknown MCP tool: " + name);
        }
    }
}
