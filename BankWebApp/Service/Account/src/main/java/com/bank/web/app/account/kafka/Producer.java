package com.bank.web.app.account.kafka;

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
public class Producer {

    @Autowired
    private KafkaTemplate<String,AccountKafkaDTO> kafkaTemplate;
    @Value("${kafkaTopic.newAccountTopic}")
    private String topic;

    @Value("${kafkaTopic.transactionTopic}")
    private String transactionTopic;

    @Value("${kafkaTopic.loan}")
    private String loanTopic;

    @Value("${kafkaTopic.recurring}")
    private String recurring;

    public void sendAccountDetails(AccountKafkaDTO AccountKafkaDTO){
        log.info("Topic : {}", topic);
        log.info("Sending notification for Account :: <{}>",AccountKafkaDTO);
        Message<AccountKafkaDTO> message = MessageBuilder
                .withPayload(AccountKafkaDTO)
                .setHeader(KafkaHeaders.TOPIC,topic)
                .build();
        kafkaTemplate.send(message);
    }

    public void sendAccountDetailsToTransaction(AccountKafkaDTO AccountKafkaDTO){
        log.info("Topic :: {}", transactionTopic);
        log.info("Sending Data for Transaction :: <{}>",AccountKafkaDTO);
        Message<AccountKafkaDTO> message = MessageBuilder
                .withPayload(AccountKafkaDTO)
                .setHeader(KafkaHeaders.TOPIC,transactionTopic)
                .build();
        kafkaTemplate.send(message);
    }

    public void sendLoanDetailsToTransaction(LoanPaymentDTO loanPaymentDTO){
        log.info("Topic ::: {}", loanTopic);
        log.info("Sending Data for Transaction(Loan) :: <{}>",loanPaymentDTO);
        Message<LoanPaymentDTO> message = MessageBuilder
                .withPayload(loanPaymentDTO)
                .setHeader(KafkaHeaders.TOPIC,loanTopic)
                .build();
        kafkaTemplate.send(message);
    }

    public void sendRecurringAccountDetails(RecurringPayment recurringPayment){
        log.info("Topic :::: {}", recurring);
        log.info("Sending Data for Recurring(Transaction) :: <{}>",recurringPayment);
        Message<RecurringPayment> message = MessageBuilder
                .withPayload(recurringPayment)
                .setHeader(KafkaHeaders.TOPIC,recurring)
                .build();
        kafkaTemplate.send(message);
    }
}
