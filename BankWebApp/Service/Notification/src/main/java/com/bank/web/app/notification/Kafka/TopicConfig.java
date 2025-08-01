package com.bank.web.app.notification.Kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TopicConfig {

    @Value("${kafkaTopic.topic}")
    private String authTopicName;

    public String getAuthTopicName() {
        return authTopicName;
    }
}
