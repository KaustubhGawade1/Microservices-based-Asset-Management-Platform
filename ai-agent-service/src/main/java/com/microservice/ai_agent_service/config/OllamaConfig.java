package com.microservice.ai_agent_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OllamaConfig {

    @Bean
    RestClient ollamaRestClient(@Value("${ollama.base-url:http://localhost:11434}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
