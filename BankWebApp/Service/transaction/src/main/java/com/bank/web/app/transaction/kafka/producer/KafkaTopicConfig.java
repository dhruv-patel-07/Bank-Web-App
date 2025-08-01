package com.bank.web.app.transaction.kafka.producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafkaTopic.topic}")
    private String loginReminder;

    @Bean
    public NewTopic reminderEmialTopic(){
        return TopicBuilder
                .name(loginReminder)
                .build();
    }
}
