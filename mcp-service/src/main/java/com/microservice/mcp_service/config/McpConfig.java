package com.microservice.mcp_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class McpConfig {

    @Bean
    RestClient assetRestClient(@Value("${asset.service.url:http://localhost:8085}") String assetServiceUrl) {
        return RestClient.builder()
                .baseUrl(assetServiceUrl)
                .build();
    }
}
