package com.microservice.asset_service.config;

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

    public static final String EXCHANGE = "asset.events";
    public static final String QUEUE = "asset.ai.processing";
    public static final String ROUTING_KEY = "asset.uploaded";

    @Bean
    TopicExchange assetEventsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue aiProcessingQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    Binding aiProcessingBinding(Queue aiProcessingQueue, TopicExchange assetEventsExchange) {
        return BindingBuilder.bind(aiProcessingQueue)
                .to(assetEventsExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
