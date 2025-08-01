package com.bank.web.app.transaction.kafka.producer;

import com.bank.web.app.transaction.dto.FreezeAccount;
import com.bank.web.app.transaction.kafka.producer.TransactionEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaProducerTransaction {

    @Autowired
    private KafkaTemplate<String, TransactionEmail> kafkaTemplate;
    @Value("${kafkaTopic.Email}")
    private String topic;

    @Value("${kafkaTopic.loanPayment}")
    private String loan;

    @Value("${kafkaTopic.loanRepayment}")
    private String reSchedule;

    @Value("${kafkaTopic.loanFailed}")
    private String failedLoanPayment;

    @Value("${kafkaTopic.freezeAccount}")
    private String freezeAccountTopic;

    public void TransactionEmail(TransactionEmail transactionEmail){
        log.info("Topic : {}", topic);
        log.info("Sending notification for Account :: <{}>",transactionEmail);
        Message<TransactionEmail> message = MessageBuilder
                .withPayload(transactionEmail)
                .setHeader(KafkaHeaders.TOPIC,topic)
                .build();
        kafkaTemplate.send(message);
    }

    public void NewLoanPaymentProducer(TransactionEmail transactionEmail){
        log.info("Topic : {}", topic);
        log.info("Sending notification for Account :: <{}>",transactionEmail);
        Message<TransactionEmail> message = MessageBuilder
                .withPayload(transactionEmail)
                .setHeader(KafkaHeaders.TOPIC,topic)
                .build();
        kafkaTemplate.send(message);
    }

    public void LoanPaymentScheduleProducer(LoanPaymentScheduleProducer loanPaymentScheduleProducer){
        log.info("Topic Schedule: {}", loan);
        log.info("Sending notification for Loan Payment :: <{}>",loanPaymentScheduleProducer);
        Message<LoanPaymentScheduleProducer> message = MessageBuilder
                .withPayload(loanPaymentScheduleProducer)
                .setHeader(KafkaHeaders.TOPIC,loan)
                .build();
        kafkaTemplate.send(message);
    }

    public void LoanRepaymentScheduleProducer(LoanPaymentScheduleProducer loanPaymentScheduleProducer){
        log.info("Topic Schedule-RePayment: {}", reSchedule);
        log.info("Sending notification for Loan Repayment :: <{}>",loanPaymentScheduleProducer);
        Message<LoanPaymentScheduleProducer> message = MessageBuilder
                .withPayload(loanPaymentScheduleProducer)
                .setHeader(KafkaHeaders.TOPIC,reSchedule)
                .build();
        kafkaTemplate.send(message);
    }

    public void LoanRepaymentFailed(LoanPaymentFailedDTO LoanPaymentFailedDTO){
        log.info("Topic Failed Payment: {}", failedLoanPayment);
        log.info("Sending notification for Loan Payment Failed :: <{}>",LoanPaymentFailedDTO);
        Message<LoanPaymentFailedDTO> message = MessageBuilder
                .withPayload(LoanPaymentFailedDTO)
                .setHeader(KafkaHeaders.TOPIC,failedLoanPayment)
                .build();
        kafkaTemplate.send(message);
    }

    public void AccountFreeze(FreezeAccount freezeAccount){
        log.info("Topic Freeze Account: {}", freezeAccount);
        log.info("Sending notification for Freeze Account :: <{}>",freezeAccount);
        Message<FreezeAccount> message = MessageBuilder
                .withPayload(freezeAccount)
                .setHeader(KafkaHeaders.TOPIC,freezeAccountTopic)
                .build();
        kafkaTemplate.send(message);
    }
}
