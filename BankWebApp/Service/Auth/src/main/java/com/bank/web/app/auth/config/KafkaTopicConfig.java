package com.bank.web.app.auth.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {


    @Value("${kafkaTopic.topic}")
    private String topic;

    @Value("${kafkaTopic.redisTopic}")
    private String redisTopic;

    @Bean
    public NewTopic authTopic(){
        return TopicBuilder
                .name(topic)
                .build();
    }
    @Bean
    public NewTopic redisTopic() {
        return TopicBuilder
                .name(redisTopic)
                .build();
    }
}
