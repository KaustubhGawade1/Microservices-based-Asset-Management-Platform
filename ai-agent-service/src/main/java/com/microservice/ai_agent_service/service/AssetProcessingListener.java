package com.microservice.ai_agent_service.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AssetProcessingListener {

    private final OllamaService ollamaService;

    public AssetProcessingListener(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @RabbitListener(queues = "asset.ai.processing")
    public void processAssetUploaded(Map<String, Object> event) {
        String prompt = "Classify this uploaded asset metadata and return only a short comma-separated tag list. "
                + "Filename: " + event.get("filename") + ". "
                + "Content type: " + event.get("contentType") + ". "
                + "Size bytes: " + event.get("size") + ".";

        String tags = ollamaService.chat(prompt);
        System.out.println("Asset " + event.get("assetId") + " generated tags: " + tags);
    }
}
