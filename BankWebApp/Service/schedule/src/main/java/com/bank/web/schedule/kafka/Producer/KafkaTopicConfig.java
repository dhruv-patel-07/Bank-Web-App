package com.bank.web.schedule.kafka.Producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

//@Configuration
public class KafkaTopicConfig {

//    @Value("${kafkaTopic.loanReminder}")
//    private String loanReminder;
//
//        @Bean
//        public NewTopic ReminderTopic(){
//            return TopicBuilder
//                    .name(loanReminder)
//                    .build();
//        }
}
