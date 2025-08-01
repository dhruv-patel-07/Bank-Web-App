package com.bank.web.app.transaction.Service;

import com.bank.web.app.transaction.Repo.AccountRepo;
import com.bank.web.app.transaction.Repo.RecurringAccountPaymentRepo;
import com.bank.web.app.transaction.Repo.TransactionRepo;
import com.bank.web.app.transaction.dto.ResponseDTO;
import com.bank.web.app.transaction.dto.TransactionDto;
import com.bank.web.app.transaction.kafka.RecurringPayment;
import com.bank.web.app.transaction.kafka.producer.KafkaProducerTransaction;
import com.bank.web.app.transaction.kafka.producer.TransactionEmail;
import com.bank.web.app.transaction.model.Account;
import com.bank.web.app.transaction.model.RecurringAccountPayment;
import com.bank.web.app.transaction.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RecurringService {

    @Autowired
    private ExtractTokenService extractTokenService;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private RecurringAccountPaymentRepo recurringAccountPaymentRepo;

    @Autowired
    private KafkaProducerTransaction kafkaProducerTransaction;


    public ResponseDTO savePayment(String authorization, TransactionDto transactionDTO) throws ParseException {
        Map<String,Object> userData =  extractTokenService.extractValue(authorization);
        String uid = userData.get("uid").toString();
        log.info("UID {}",uid);
//        Account account = accountRepo.findByAccountNum(transactionDTO.getAccountNumber());
        Account account = accountRepo.findByUidAndAccountNum(uid,transactionDTO.getAccountNumber());
        log.info("account :: {}",account.getAccountType());
        if(account != null && account.getAccountType().equalsIgnoreCase("RECURRING")){
            List<RecurringAccountPayment> rpList = recurringAccountPaymentRepo.findByAccountNum(transactionDTO.getAccountNumber(), Sort.by(Sort.Direction.DESC,"pId"));
            RecurringAccountPayment rp = rpList.get(0);
            if(rp.getDueAmount() != transactionDTO.getAmount()){
                return new ResponseDTO("200","Payment amount must be "+rp.getDueAmount(),null);
            }
            if(rp.getMonth() == rp.getMonthsCompleted())
            {
                return new ResponseDTO("200","The recurring account tenure has been completed ",null);
            }
            LocalDate today = LocalDate.now();                  // e.g., 2025-07-17
            LocalDate scheduledDate = rp.getScheduleDate();     // the date you want to check
            LocalDate minAllowedDate = scheduledDate.minusDays(2);      // 2025-07-15
            LocalDate maxAllowedDate = scheduledDate.plusDays(2);       // 2025-07-19
            log.info("Scheduled date :: {}",scheduledDate);
            log.info("Start date :: {}",minAllowedDate);
            log.info("end date :: {}",maxAllowedDate);

            if (!today.isBefore(minAllowedDate) && !today.isAfter(maxAllowedDate)) {
                rp.setPaymentAmount(transactionDTO.getAmount());
                rp.setDueAmount(0.0);
                rp.setMonthsCompleted(rp.getMonthsCompleted() + 1);
                rp.setPaidAmount(transactionDTO.getAmount() + rp.getPaidAmount());
                rp.setPaymentDate(LocalDateTime.now());

                Transaction transaction = new Transaction();
                transaction.setTransactionType("credit");
                transaction.setAmount(transactionDTO.getAmount());
                transaction.setTransactionMethod("recurring-payment");
                transaction.setAffected_balance(account.getBalance() + rp.getSipAmount());
                transaction.setTimeStamp(LocalDateTime.now());
                transaction.setRemark("Recurring payment " + rp.getMonthsCompleted() + 1);
                transaction.setRecurringAccountPayment(rp);
                rp.setTransaction(transaction);
                var t1 = transactionRepo.save(transaction);

                RecurringAccountPayment nextPayment = new RecurringAccountPayment();
                nextPayment.setDueAmount(rp.getSipAmount());
                nextPayment.setAccountNum(rp.getAccountNum());
                nextPayment.setEmail(rp.getEmail());
                nextPayment.setInterestRate(rp.getInterestRate());
                nextPayment.setMonth(rp.getMonth());
                nextPayment.setMonthsCompleted(rp.getMonthsCompleted());
                nextPayment.setPaymentAmount(rp.getSipAmount());
                nextPayment.setRacId(rp.getRacId());
                nextPayment.setSipAmount(rp.getSipAmount());
                nextPayment.setPaidAmount(rp.getPaidAmount());

                nextPayment.setScheduleDate(rp.getScheduleDate().plusMonths(1));
                nextPayment.setTotalAmount(rp.getTotalAmount());
                recurringAccountPaymentRepo.save(nextPayment);
//            rp.setTransaction(t1);
                Map<String, String> output = new HashMap<>();
                output.put("TID", t1.getTId().toString());
                output.put("PaymentMonth", String.valueOf(rp.getMonthsCompleted()));
                output.put("Amount", String.valueOf(transaction.getAmount()));


                TransactionEmail transactionEmail = new TransactionEmail();
                transactionEmail.setEmail(userData.get("email").toString());
                transactionEmail.setAccount(transactionDTO.getAccountNumber().toString());
                transactionEmail.setAmount(String.valueOf(transactionDTO.getAmount()));
                transactionEmail.setTime(t1.getTimeStamp().toString());
                transactionEmail.setMethod(t1.getTransactionMethod());
                transactionEmail.setType("credit");
                transactionEmail.setTid(t1.getTId().toString());
                transactionEmail.setAcType(account.getAccountType());
                kafkaProducerTransaction.TransactionEmail(transactionEmail);

                return new ResponseDTO("200", "Payment success", output);
            }else {
                if(today.isAfter(maxAllowedDate)) {

                    Double due = rp.getDueAmount();
                    Double dueAmount = due * 0.04d;
                    System.out.println(dueAmount + " <-  Due "+"amount -> "+rp.getDueAmount());
                    rp.setDueAmount(dueAmount + rp.getDueAmount() +rp.getSipAmount());
                    rp.setScheduleDate(rp.getScheduleDate().plusMonths(1));
                    rp.setMonthsCompleted(rp.getMonthsCompleted()+1);
                    recurringAccountPaymentRepo.save(rp);
                    return new ResponseDTO("200", "Payment window has expired. Last allowed date was " + maxAllowedDate +" Pay with Penalty "+dueAmount,null );
                }
                return new ResponseDTO("200", "Date must between "+minAllowedDate+" TO "+maxAllowedDate,null );
            }
        }
        return new ResponseDTO("401","Account not found",null);

    }
}
