package com.bank.web.schedule.kafka.Consumer;

import com.bank.web.schedule.Repo.AccountsRepo;
import com.bank.web.schedule.Repo.LoanPaymentRepo;
import com.bank.web.schedule.elasticSearch.DLQ;
import com.bank.web.schedule.elasticSearch.DLQRepo;
import com.bank.web.schedule.model.Accounts;
import com.bank.web.schedule.model.LoanPayment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class ConsumeTopic {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private LoanPaymentRepo loanPaymentRepo;

    @Autowired
    private AccountsRepo accountsRepo;

    @Autowired
    private DLQRepo dlqRepo;

    @KafkaListener(topics = "${KafkaTopic.topic}")
    public void consumeLoanTopic(LoanPaymentScheduleProducer loanPaymentScheduleProducer, Acknowledgment ack) throws MessagingException {
        log.info("Consuming the message from Auth-Topic:: {} ", loanPaymentScheduleProducer);

        int retries = 3;
        String Exc = "";
        while (retries-- > 0) {
            try {
                LoanPayment payment = new LoanPayment();
                payment.setLoanId(loanPaymentScheduleProducer.getLoanId());
                payment.setEmi(loanPaymentScheduleProducer.getEmi());
                payment.setBalance(0.0d);
                payment.setScheduleDate(loanPaymentScheduleProducer.getDate().toString());
                payment.setPaymentId(loanPaymentScheduleProducer.getPaymentId());
                payment.setEmail(loanPaymentScheduleProducer.getEmail());
                payment.setIsReminderSend(false);
                loanPaymentRepo.save(payment);
//            Commit Kafka Topic
                ack.acknowledge();
                System.out.println("Loan Payment Schedule Success");
                break;
            } catch (Exception e) {
                log.warn("Error While Consuming Topic :: {}", e.getMessage());
            }
        }
        log.info("Save in NEW LOAN DLQ.ERR");
        DLQ dlq = new DLQ();
        dlq.setTittle("LoanPayment.DLQ");
        dlq.setObject(loanPaymentScheduleProducer);
        dlq.setException(Exc);
        dlq.setTimestamp(LocalDateTime.now());
        dlqRepo.save(dlq);
        kafkaTemplate.send("newLoanPayment.DLQ",loanPaymentScheduleProducer);
    }

    @KafkaListener(topics = "${KafkaTopic.loanRepayment}")
    public void consumeLoanUpdateTopic(LoanPaymentScheduleProducer loanPaymentScheduleProducer,Acknowledgment ack) throws MessagingException {
        log.info("Consuming the message from Loan-Repayment-Topic:: {} ", loanPaymentScheduleProducer);

        int retries = 3;
        String Exc = "";
        while (retries-- > 0) {
            try {
                log.info("loan-payment retries-{}",retries);
//                if(true){
//                    throw new RuntimeException("🔥 Fake exception to trigger DLQ");
//                }
                LoanPayment payment = loanPaymentRepo.findByLoanId(loanPaymentScheduleProducer.getLoanId());
                payment.setEmi(loanPaymentScheduleProducer.getEmi());
                payment.setBalance(loanPaymentScheduleProducer.getBalance());
                payment.setScheduleDate(loanPaymentScheduleProducer.getDate().toString());
                payment.setPaymentId(loanPaymentScheduleProducer.getPaymentId());
                payment.setIsReminderSend(false);
                loanPaymentRepo.save(payment);
                ack.acknowledge();
                System.out.println("Loan Payment-2  Success");
                return;

            } catch (Exception e) {
                log.warn("Error While Consuming Topic :: {}", e.getMessage());
                Exc = e.getMessage();
            }
        }
        log.info("Save in LOAN DLQ.ERROR");
        DLQ dlq = new DLQ();
        dlq.setTittle("LoanPayment.DLQ");
        dlq.setObject(loanPaymentScheduleProducer);
        dlq.setException(Exc);
        dlq.setTimestamp(LocalDateTime.now());
        dlqRepo.save(dlq);
        kafkaTemplate.send("LoanPayment.DLQ",loanPaymentScheduleProducer);
    }

    @KafkaListener(topics = "${KafkaTopic.transactionTopic}")
    public void consumeAccountTopic(AccountKafkaDTO accountKafkaDTO,Acknowledgment ack) throws MessagingException {
        log.info("Consuming the message from Account-Topic:: {} ", accountKafkaDTO);
        String Exc = "";
        int retries = 3;
        while (retries-- > 0) {
            try {
                Accounts accounts = new Accounts();
                accounts.setAccountNum(accountKafkaDTO.getAccountNum());
                accounts.setBranchId(accountKafkaDTO.getBranchId());
                accounts.setType(accountKafkaDTO.getAccountType());
                accountsRepo.save(accounts);
                ack.acknowledge();
                System.out.println("Account Topic Success ");
                return;
            } catch (Exception e) {
                log.warn("Error While Consuming Account Topic Topic :: {}", e.getMessage());
            }
        }
        log.info("Save in ACCOUNT DLQ.ERROR");
        DLQ dlq = new DLQ();
        dlq.setTittle("LoanPayment.DLQ");
        dlq.setObject(accountKafkaDTO);
        dlq.setException(Exc);
        dlq.setTimestamp(LocalDateTime.now());
        dlqRepo.save(dlq);
        kafkaTemplate.send("account.DLQ",accountKafkaDTO);
    }
}
