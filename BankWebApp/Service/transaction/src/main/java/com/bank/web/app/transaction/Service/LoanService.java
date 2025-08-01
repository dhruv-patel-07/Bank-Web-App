package com.bank.web.app.transaction.Service;

import com.bank.web.app.transaction.Repo.AccountRepo;
import com.bank.web.app.transaction.Repo.LoanPaymentRepo;
import com.bank.web.app.transaction.dto.ResponseDTO;
import com.bank.web.app.transaction.kafka.producer.KafkaProducerTransaction;
import com.bank.web.app.transaction.kafka.producer.LoanPaymentFailedDTO;
import com.bank.web.app.transaction.kafka.producer.LoanPaymentScheduleProducer;
import com.bank.web.app.transaction.kafka.producer.TransactionEmail;
import com.bank.web.app.transaction.model.Account;
import com.bank.web.app.transaction.model.LoanPayment;
import com.bank.web.app.transaction.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class LoanService {

    @Autowired
    private LoanPaymentRepo loanPaymentRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private KafkaProducerTransaction kafkaProducerTransaction;

    public void deductEmi(List<Long> pidList) {
        log.info("data received {}",pidList);
        List<LoanPayment> loanPayment = loanPaymentRepo.findAllByLpIdIn(pidList);
        for (LoanPayment payment : loanPayment) {
            Account account = accountRepo.findByAccountNum(payment.getAccountNum());
            LoanPaymentScheduleProducer scheduleProducer = new LoanPaymentScheduleProducer();

            if (payment.getPaymentAmount() == 0.0d) {
                log.info("Return from 1 if");
                return;
            }
//            Date Optional
            if(!payment.getScheduleDate().equals(LocalDate.now())){
                log.info("Date mismatch");
                return;

            }
            if (payment.getDueAmount() != 0) {
                log.info("Return from 2 if");
                if (account.getBalance() > payment.getDueAmount()) {
                    log.info("Return from 3 if");
                    double paymentAmount = payment.getDueAmount();
                    payment.setDueAmount(0.0);
                    payment.setPaymentDate(LocalDateTime.now());
                    payment.setPaidAmount(paymentAmount);
//                Add to Transaction
                    Transaction transaction = new Transaction();
                    transaction.setTransactionType("debit");
                    transaction.setTransactionMethod("loan-payment");
                    transaction.setAmount(paymentAmount);
                    transaction.setTimeStamp(LocalDateTime.now());
                    transaction.setAffected_balance(account.getBalance() - paymentAmount);
                    transaction.setAccount(account);
                    transaction.setLoanPayment(payment);
                    transaction.setRemark("loan-payment-" + payment.getLoanNum());
                    payment.setTransaction(transaction);
                    var lpResponse = loanPaymentRepo.save(payment);
                    account.setBalance(Math.round((account.getBalance() - paymentAmount) * 100.00) / 100.00);
                    accountRepo.save(account);

                    LoanPayment newloanPayment = new LoanPayment();
                    newloanPayment.setLoanNum(payment.getLoanNum());
                    newloanPayment.setPaymentAmount(Math.round((payment.getPaymentAmount() - paymentAmount) * 100.00) / 100.00);
                    newloanPayment.setPrincipleAmount(payment.getPrincipleAmount());
                    newloanPayment.setInterestAmount(payment.getInterestAmount());
                    newloanPayment.setPaidAmount(0.0d);
                    newloanPayment.setDueAmount(payment.getEmiAmount());
                    newloanPayment.setEmiAmount(payment.getEmiAmount());
                    newloanPayment.setAccountNum(payment.getAccountNum());
                    newloanPayment.setScheduleDate(LocalDate.now().plusDays(30));
                    var nlp = loanPaymentRepo.save(newloanPayment);
//                Email
                    TransactionEmail transactionEmail = new TransactionEmail();
                    transactionEmail.setTime(LocalDateTime.now().toString());
                    transactionEmail.setType("debit");
                    transactionEmail.setTid(lpResponse.getTransaction().getTId().toString());
                    transactionEmail.setEmail(payment.getTransaction().getAccount().getEmail());
                    transactionEmail.setMethod("loan-payment," + payment.getLoanNum());
                    transactionEmail.setAccount(payment.getLoanNum().toString());
                    transactionEmail.setAmount(String.valueOf(paymentAmount));
                    log.info("Payment Success");
                    kafkaProducerTransaction.TransactionEmail(transactionEmail);
                    scheduleProducer.setBalance(0.0);
                    scheduleProducer.setEmi(payment.getEmiAmount());
                    scheduleProducer.setLoanId(payment.getLoanNum());
                    scheduleProducer.setDate(nlp.getScheduleDate());
                    scheduleProducer.setPaymentId(nlp.getLpId());
                    kafkaProducerTransaction.LoanRepaymentScheduleProducer(scheduleProducer);

                } else {
                    log.info("Due amount{}",payment.getDueAmount());
                    payment.setDueAmount(payment.getDueAmount() + payment.getEmiAmount());
                    payment.setScheduleDate(LocalDate.now().plusDays(30));
                    scheduleProducer.setEmi(Math.round((payment.getDueAmount() - payment.getEmiAmount()) * 100.00) / 100.00);
                    scheduleProducer.setDate(LocalDate.now().plusDays(30));
                    scheduleProducer.setPaymentId(payment.getLpId());
                    scheduleProducer.setLoanId(payment.getLoanNum());
                    loanPaymentRepo.save(payment);
                    kafkaProducerTransaction.LoanRepaymentScheduleProducer(scheduleProducer);

                    LoanPaymentFailedDTO loanPaymentFailedDTO = new LoanPaymentFailedDTO();
                    loanPaymentFailedDTO.setLoanNum(payment.getLoanNum());
                    loanPaymentFailedDTO.setEmail(account.getEmail());
                    loanPaymentFailedDTO.setNextDate(LocalDate.now().plusDays(30));
                    loanPaymentFailedDTO.setAmount(Math.round((payment.getDueAmount() - payment.getEmiAmount()) * 100.00) / 100.00);
                    loanPaymentFailedDTO.setAccountNum(payment.getAccountNum());
                    kafkaProducerTransaction.LoanRepaymentFailed(loanPaymentFailedDTO);
                    log.warn("Loan payment failed.....");
//                Kafka And email
                }
            }
        }
    }
}
