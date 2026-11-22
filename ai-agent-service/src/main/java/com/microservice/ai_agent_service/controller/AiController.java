package com.microservice.ai_agent_service.controller;

import com.microservice.ai_agent_service.service.OllamaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final OllamaService ollamaService;

    public AiController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(ollamaService.chatWithMcp(request.message()));
    }

    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse ollamaUnavailable(RestClientException exception) {
        return new ErrorResponse("Ollama is unavailable or the configured model is not installed.");
    }

    public record ChatRequest(@NotBlank String message) {}

    public record ChatResponse(String answer) {}

    public record ErrorResponse(String error) {}
}
