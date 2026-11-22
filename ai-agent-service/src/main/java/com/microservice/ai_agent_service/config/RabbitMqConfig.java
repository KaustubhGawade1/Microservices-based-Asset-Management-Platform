package com.microservice.ai_agent_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    @Bean
    TopicExchange assetEventsExchange() {
        return new TopicExchange("asset.events", true, false);
    }

    @Bean
    Queue aiProcessingQueue() {
        return new Queue("asset.ai.processing", true);
    }

    @Bean
    Binding aiProcessingBinding(Queue aiProcessingQueue, TopicExchange assetEventsExchange) {
        return BindingBuilder.bind(aiProcessingQueue)
                .to(assetEventsExchange)
                .with("asset.uploaded");
    }

    @Bean
    MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
