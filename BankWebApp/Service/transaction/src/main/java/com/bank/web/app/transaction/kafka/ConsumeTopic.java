package com.bank.web.app.transaction.kafka;

import com.bank.web.app.transaction.Elasticsearch.ElasticSaveService;
import com.bank.web.app.transaction.Elasticsearch.UserDetails;
import com.bank.web.app.transaction.Repo.AccountRepo;
import com.bank.web.app.transaction.Service.SaveService;
import com.bank.web.app.transaction.dto.AddInterestDTO;
import com.bank.web.app.transaction.dto.TransactionDto;
import com.bank.web.app.transaction.model.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConsumeTopic {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private SaveService saveService;

    @Autowired
    private ElasticSaveService elasticSaveService;

    @KafkaListener(topics = "${kafkaTopic.topic}")
    public void consumeTransactionTopic(AccountKafkaDTO accountKafkaDTO){
        log.info("Consuming the message from Transaction-Topic:: {} ",accountKafkaDTO);
        try{
           saveService.save(accountKafkaDTO);
            UserDetails userDetails = new UserDetails();
            userDetails.setName(accountKafkaDTO.getName());
            userDetails.setEmail(accountKafkaDTO.getEmail());
            userDetails.setAccountNumber(accountKafkaDTO.getAccountNum());
            userDetails.setUid(accountKafkaDTO.getUid());
           elasticSaveService.SaveToElastic(userDetails);

        }catch (Exception e){
            log.warn("Exception :: {}",e.getMessage());
        }
    }

    @KafkaListener(topics = "${kafkaTopic.loan}")
    public void consumeLoanTopic(LoanPaymentDTO loanPaymentDTO){
        log.info("Consuming the message from Loan-Topic:: {} ",loanPaymentDTO);
        try{
            saveService.saveLoan(loanPaymentDTO);
            log.info("Loan PaymentSaved");
        }catch (Exception e){
            log.warn("Exception Loan :: {}",e.getMessage());
        }
    }

    @KafkaListener(topics = "${kafkaTopic.interest}")
    public void consumeInterestTopic(AddInterestDTO addInterestDTO){
        log.info("Consuming the message from Interest-Rate-Topic:: {} ",addInterestDTO);
        try{
            saveService.addInterest(addInterestDTO);
            log.info("Interest PaymentSaved");
        }catch (Exception e){
            log.warn("Exception Interest :: {}",e.getMessage());
        }
    }

    @KafkaListener(topics = "${kafkaTopic.recurring}")
    public void consumeRecurringTopic(RecurringPayment recurringPayment){
        log.info("Consuming the message from Recurring-Topic:: {} ",recurringPayment);
        try{
            saveService.SaveRecurringPayment(recurringPayment);
            log.info("Recurring PaymentSaved");
        }catch (Exception e){
            log.warn("Exception Recurring :: {}",e.getMessage());
        }
    }
}
