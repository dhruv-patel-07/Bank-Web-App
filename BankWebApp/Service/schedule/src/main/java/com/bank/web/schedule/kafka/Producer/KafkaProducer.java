package com.bank.web.schedule.kafka.Producer;

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
public class KafkaProducer {


    @Autowired
    private KafkaTemplate<String,LoanPaymentFailedDTO> kafkaTemplate;

    @Value("${KafkaTopic.loanReminder}")  // ✅ All lowercase
    String loanReminder;

    @Value("${KafkaTopic.statementEmail}")  // ✅ All lowercase
    String emil;

    @Value("${KafkaTopic.interest}")  // ✅ All lowercase
    String interest;




    public void ReminderEmail(LoanPaymentFailedDTO loanPaymentFailedDTO){
        log.info("Topic : {}", loanReminder);
        log.info("Sending notification for Email :: <{}>",loanPaymentFailedDTO);
        Message<LoanPaymentFailedDTO> message = MessageBuilder
                .withPayload(loanPaymentFailedDTO)
                .setHeader(KafkaHeaders.TOPIC,loanReminder)
                .build();
        kafkaTemplate.send(message);
    }

    public void StatementEmail(EmailWithAttachmentDTO emailWithAttachmentDTO){
        log.info("Topic Email-Attachment-Topic : {}", emil);
        log.info("Sending notification for Email-With-Attachment :: <{}>",emailWithAttachmentDTO);
        Message<EmailWithAttachmentDTO> message = MessageBuilder
                .withPayload(emailWithAttachmentDTO)
                .setHeader(KafkaHeaders.TOPIC,emil)
                .build();
        kafkaTemplate.send(message);
    }

    public void InterestRateService(AddInterestDTO addInterestDTO){
        log.info("Topic Interest-Rate-Topic : {}", emil);
        log.info("Sending notification for Interest-Rate :: <{}>",addInterestDTO);
        Message<AddInterestDTO> message = MessageBuilder
                .withPayload(addInterestDTO)
                .setHeader(KafkaHeaders.TOPIC,interest)
                .build();
        kafkaTemplate.send(message);
    }



}
