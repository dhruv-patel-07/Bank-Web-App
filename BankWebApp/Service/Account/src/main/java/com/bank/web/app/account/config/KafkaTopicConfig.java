package com.bank.web.app.account.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

public class KafkaTopicConfig {

    @Value("${kafkaTopic.newAccountTopic}")
    private String topic;

    @Value("${kafkaTopic.transactionTopic}")
    private String transactionTopic;

    @Value("${kafkaTopic.loan}")
    private String loan;

    @Bean
    public NewTopic authTopic(){
        return TopicBuilder
                .name(topic)
                .build();
    }

    @Bean
    public NewTopic transactionTopic(){
        return TopicBuilder
                .name(transactionTopic)
                .build();
    }

    @Bean
    public NewTopic LoanTopic(){
        return TopicBuilder
                .name(loan)
                .build();
    }
}
