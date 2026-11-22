package com.microservice.ai_agent_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OllamaService {

    private final RestClient ollamaRestClient;
    private final RestClient mcpRestClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public OllamaService(
            RestClient ollamaRestClient,
            RestClient mcpRestClient,
            ObjectMapper objectMapper,
            @Value("${ollama.model:llama3.2:3b}") String model) {
        this.ollamaRestClient = ollamaRestClient;
        this.mcpRestClient = mcpRestClient;
        this.objectMapper = objectMapper;
        this.model = model;
    }

    public String chat(String message) {
        return chatWithMessages(List.of(
                new ChatMessage("system", "You are a helpful assistant for an asset management platform. Answer concisely and do not invent file contents."),
                new ChatMessage("user", message)
        ));
    }

    public String chatWithMcp(String message) {
        String toolPrompt = "Choose at most one MCP tool for this request. "
                + "Available tools: search_assets(query) and list_public_assets(). "
                + "Return only JSON with either {\"tool\":\"search_assets\",\"arguments\":{\"query\":\"...\"}}, "
                + "{\"tool\":\"list_public_assets\",\"arguments\":{}}, or {\"tool\":null,\"arguments\":{}}. "
                + "Do not add Markdown or explanations. Request: " + message;

        String toolDecision = chat(toolPrompt);
        JsonNode decision;

        try {
            decision = objectMapper.readTree(toolDecision);
        } catch (Exception exception) {
            decision = objectMapper.createObjectNode().putNull("tool");
        }

        JsonNode toolNode = decision.get("tool");
        if (toolNode == null || toolNode.isNull()) {
            return chat(message);
        }

        String toolName = toolNode.asText();
        if (!"search_assets".equals(toolName) && !"list_public_assets".equals(toolName)) {
            return chat(message);
        }

        Map<String, Object> arguments = new LinkedHashMap<>();
        JsonNode argumentsNode = decision.get("arguments");
        if (argumentsNode != null && argumentsNode.isObject()) {
            argumentsNode.properties().forEach(entry -> arguments.put(entry.getKey(), entry.getValue().asText()));
        }

        Map<String, Object> params = Map.of("name", toolName, "arguments", arguments);
        Map<String, Object> request = Map.of(
                "jsonrpc", "2.0",
                "id", 1,
                "method", "tools/call",
                "params", params
        );

        Map<String, Object> safeRequest = request;
        JsonNode mcpResponse = mcpRestClient.post()
                .uri("/mcp")
                .body((Object) Objects.requireNonNull(safeRequest))
                .retrieve()
                .body(JsonNode.class);

        String toolResult = extractMcpText(mcpResponse);
        return chatWithMessages(List.of(
                new ChatMessage("system", "Answer only from the MCP tool result below. If it is empty or an error, say that no matching assets were found."),
                new ChatMessage("user", "Question: " + message),
                new ChatMessage("assistant", "MCP result: " + toolResult)
        ));
    }

    private String chatWithMessages(List<ChatMessage> messages) {
        ChatRequest request = new ChatRequest(model, messages, false);

        ChatResponse response = ollamaRestClient.post()
                .uri("/api/chat")
                .body(request)
                .retrieve()
                .body(ChatResponse.class);

        if (response == null || response.message() == null) {
            throw new IllegalStateException("Ollama returned an empty response");
        }

        return response.message().content();
    }

    private String extractMcpText(JsonNode mcpResponse) {
        if (mcpResponse == null) {
            return "[]";
        }

        JsonNode content = mcpResponse.path("result").path("content");
        if (content.isArray() && content.has(0)) {
            return content.get(0).path("text").asText("[]");
        }

        return mcpResponse.path("error").path("message").asText("[]");
    }

    public record ChatRequest(String model, List<ChatMessage> messages, boolean stream) {}

    public record ChatMessage(String role, String content) {}

    public record ChatResponse(ChatMessage message) {}
}
