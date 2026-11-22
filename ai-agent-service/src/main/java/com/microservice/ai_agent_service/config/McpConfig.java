package com.microservice.ai_agent_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Configuration
public class McpConfig {

    @Bean
    RestClient mcpRestClient(@Value("${mcp.service.url:http://localhost:8092}") String mcpServiceUrl) {
        return RestClient.builder()
                .baseUrl(Objects.requireNonNull(mcpServiceUrl))
                .build();
    }
}
