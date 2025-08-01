package com.bank.web.app.download.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafkaTopic.requestStatement}")
    private String topic;

    public void sendStatementEmail(EmailWithAttachmentDTO emailWithAttachmentDTO) {
        log.info("Kafka Topic: {}", topic);
        log.info("Sending email with attachment: <{}>", emailWithAttachmentDTO);

        Message<EmailWithAttachmentDTO> message = MessageBuilder
                .withPayload(emailWithAttachmentDTO)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();

        kafkaTemplate.send(message);
    }
}
