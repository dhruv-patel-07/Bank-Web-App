package com.bank.web.app.transaction.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.stereotype.Component;

@Component
public class TopicConfig {

    @Value("${kafkaTopic.Email}")
    private String transactionTopicName;

    public String getAuthTopicName() {
        return transactionTopicName;
    }
    @Bean
    public NewTopic transactionTopic(){
        return TopicBuilder
                .name(transactionTopicName)
                .partitions(2)
                .build();
    }
}
