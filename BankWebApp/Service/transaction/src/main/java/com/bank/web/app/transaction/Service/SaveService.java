package com.bank.web.app.transaction.Service;

import com.bank.web.app.transaction.Repo.AccountRepo;
import com.bank.web.app.transaction.Repo.LoanPaymentRepo;
import com.bank.web.app.transaction.Repo.RecurringAccountPaymentRepo;
import com.bank.web.app.transaction.Repo.TransactionRepo;
import com.bank.web.app.transaction.dto.AddInterestDTO;
import com.bank.web.app.transaction.kafka.AccountKafkaDTO;
import com.bank.web.app.transaction.kafka.LoanPaymentDTO;
import com.bank.web.app.transaction.kafka.RecurringPayment;
import com.bank.web.app.transaction.kafka.producer.KafkaProducerTransaction;
import com.bank.web.app.transaction.kafka.producer.LoanPaymentScheduleProducer;
import com.bank.web.app.transaction.kafka.producer.TransactionEmail;
import com.bank.web.app.transaction.model.Account;
import com.bank.web.app.transaction.model.LoanPayment;
import com.bank.web.app.transaction.model.RecurringAccountPayment;
import com.bank.web.app.transaction.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class SaveService {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private LoanPaymentRepo loanPaymentRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private KafkaProducerTransaction kafkaProducerTransaction;

    @Autowired
    private RecurringAccountPaymentRepo recurringAccountPaymentRepo;

    @Async
    public CompletableFuture<Void> save(AccountKafkaDTO accountKafkaDTO){
        try {
            Account account = new Account();
            account.setAccountNum(accountKafkaDTO.getAccountNum());
            account.setAccountType(accountKafkaDTO.getAccountType());
            account.setEmail(accountKafkaDTO.getEmail());
            account.setBranchId(accountKafkaDTO.getBranchId());
            account.setUid(accountKafkaDTO.getUid());
            account.setFreeze(false);
            account.setActive(true);
            account.setName(accountKafkaDTO.getName());
            account.setBalance(accountKafkaDTO.getBalance());
            account.setBranchName(accountKafkaDTO.getBranchName());
            accountRepo.save(account);
            return CompletableFuture.completedFuture(null);
        }
        catch (Exception e){
            log.error("Error saving account: ", e);
            return CompletableFuture.failedFuture(e);

        }
    }
    @Async
    public CompletableFuture<Void> saveLoan(LoanPaymentDTO loanPaymentDTO) {
        try {
            LoanPayment payment = new LoanPayment();
            payment.setAccountNum(loanPaymentDTO.getAccountNum());
            payment.setDueAmount(loanPaymentDTO.getDueAmount());
            payment.setEmiAmount(loanPaymentDTO.getEmiAmount());
            payment.setInterestAmount(loanPaymentDTO.getInterestAmount());
            payment.setPaidAmount(loanPaymentDTO.getPaidAmount());
            payment.setPrincipleAmount(loanPaymentDTO.getPrincipleAmount());
            payment.setLoanNum(loanPaymentDTO.getLoan());
            payment.setScheduleDate(loanPaymentDTO.getScheduleDate());
            payment.setPaymentAmount(loanPaymentDTO.getPaymentAmount());
            var data = loanPaymentRepo.save(payment);
            LoanPaymentScheduleProducer loanPaymentScheduleProducer = new LoanPaymentScheduleProducer();
            loanPaymentScheduleProducer.setLoanId(data.getLoanNum());
            loanPaymentScheduleProducer.setDate(data.getScheduleDate());
            loanPaymentScheduleProducer.setEmi(data.getEmiAmount());
            loanPaymentScheduleProducer.setBalance(0.0);
            loanPaymentScheduleProducer.setPaymentId(data.getLpId());
            loanPaymentScheduleProducer.setEmail(loanPaymentDTO.getEmail());
            kafkaProducerTransaction.LoanPaymentScheduleProducer(loanPaymentScheduleProducer);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Error saving loan Payment: ", e);
            return CompletableFuture.failedFuture(e);

        }
    }
    @Async
    public void addInterest(AddInterestDTO addInterestDTO){
        Account account = accountRepo.findByAccountNum(addInterestDTO.getAccountNumber());
        Transaction transaction = new Transaction();
        transaction.setRemark(addInterestDTO.getRemark());
        transaction.setAmount(addInterestDTO.getAmount());
        double updatedBalance = Math.round((account.getBalance() + addInterestDTO.getAmount()) * 100.00) / 100.00;
        transaction.setAffected_balance(updatedBalance);
        transaction.setTransactionMethod("neft");
        transaction.setTransactionType("credit");
        transaction.setTimeStamp(LocalDateTime.now());
        account.setBalance(updatedBalance);
        transaction.setAccount(account);
        var response = transactionRepo.save(transaction);

        TransactionEmail transactionEmail = new TransactionEmail();
        transactionEmail.setEmail(account.getEmail());
        transactionEmail.setAccount(addInterestDTO.getAccountNumber().toString());
        transactionEmail.setAmount(String.valueOf(Math.round((addInterestDTO.getAmount()) * 100.00) / 100.00));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-yy-MM HH:mm");
        String formattedTime = response.getTimeStamp().format(formatter);
        transactionEmail.setTime(formattedTime);
        transactionEmail.setMethod(response.getTransactionMethod());
        transactionEmail.setType("credit");
        transactionEmail.setTid(response.getTId().toString());
        kafkaProducerTransaction.TransactionEmail(transactionEmail);
    }

    @Async
    public void SaveRecurringPayment(RecurringPayment recurringPayment){
        RecurringAccountPayment rap = new RecurringAccountPayment();
        rap.setAccountNum(recurringPayment.getAccountNum());
        rap.setEmail(recurringPayment.getEmail());
        rap.setPaymentAmount(recurringPayment.getPaymentAmount());
        rap.setDueAmount(recurringPayment.getDueAmount());
        rap.setMonth(recurringPayment.getMonth());
        rap.setScheduleDate(recurringPayment.getScheduleDate());
        rap.setSipAmount(recurringPayment.getSipAmount());
        rap.setRacId(recurringPayment.getRacId());
        rap.setTotalAmount(recurringPayment.getTotalAmount());
        rap.setInterestRate(recurringPayment.getInterestRate());
        rap.setPaidAmount(0.0);
        recurringAccountPaymentRepo.save(rap);
    }

}
