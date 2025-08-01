package com.bank.web.app.auth.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthLinkProducer {

    @Autowired
    private KafkaTemplate<String,AuthDto> kafkaTemplate;
    @Value("${kafkaTopic.topic}")
    private String topic;

    @Value("${kafkaTopic.redisTopic}")
    private String redisTopic;

    public void sendAuthLink(AuthDto authDto){
        log.info("Topic : {}", topic);
        log.info("Sending notification for Auth <{}>",authDto);
        Message<AuthDto> message = MessageBuilder
                .withPayload(authDto)
                .setHeader(KafkaHeaders.TOPIC,topic)
                .build();
        kafkaTemplate.send(message);
    }

    public void sendRefreshToken(RedisTokenDto redisTokenDto){
        log.info("RedisTopic : {} ",redisTopic);
        log.info("Sending refresh token :: {}",redisTokenDto.toString());
        Message<RedisTokenDto> message = MessageBuilder
                .withPayload(redisTokenDto)
                .setHeader(KafkaHeaders.TOPIC,redisTopic)
                .build();
        kafkaTemplate.send(message);
    }
}
