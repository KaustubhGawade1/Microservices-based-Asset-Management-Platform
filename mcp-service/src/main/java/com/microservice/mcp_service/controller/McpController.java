package com.microservice.mcp_service.controller;

import com.microservice.mcp_service.service.McpToolService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/mcp")
public class McpController {

    private final McpToolService toolService;

    public McpController(McpToolService toolService) {
        this.toolService = toolService;
    }

    @PostMapping
    public Map<String, Object> handle(@RequestBody Map<String, Object> request) {
        Object id = request.get("id");
        String method = String.valueOf(request.get("method"));

        if ("initialize".equals(method)) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("protocolVersion", "2025-06-18");
            result.put("capabilities", Map.of("tools", Map.of()));
            result.put("serverInfo", Map.of("name", "asset-management-mcp", "version", "1.0.0"));
            return success(id, result);
        }

        if ("tools/list".equals(method)) {
            return success(id, Map.of("tools", toolService.tools()));
        }

        if ("tools/call".equals(method)) {
            Map<String, Object> params = asMap(request.get("params"));
            String name = String.valueOf(params.get("name"));
            Map<String, Object> arguments = asMap(params.get("arguments"));

            try {
                return success(id, toolService.call(name, arguments));
            } catch (McpToolService.UnknownToolException exception) {
                return error(id, -32602, exception.getMessage());
            }
        }

        return error(id, -32601, "Method not found: " + method);
    }

    private Map<String, Object> success(Object id, Object result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("result", result);
        return response;
    }

    private Map<String, Object> error(Object id, int code, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jsonrpc", "2.0");
        response.put("id", id);
        response.put("error", Map.of("code", code, "message", message));
        return response;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }
}
