package com.bank.web.app.transaction.Service;

import com.bank.web.app.transaction.Repo.AccountRepo;
import com.bank.web.app.transaction.Repo.LoanPaymentRepo;
import com.bank.web.app.transaction.Repo.TransactionRepo;
import com.bank.web.app.transaction.dto.*;
import com.bank.web.app.transaction.enums.StatusCodeEnum;
import com.bank.web.app.transaction.kafka.producer.KafkaProducerTransaction;
import com.bank.web.app.transaction.kafka.producer.TransactionEmail;
import com.bank.web.app.transaction.model.Account;
import com.bank.web.app.transaction.model.LoanPayment;
import com.bank.web.app.transaction.model.Transaction;
import com.netflix.discovery.converters.Auto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private ExtractTokenService extractTokenService;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private TransactionRepo transactionRepo;

    @Autowired
    private LoanPaymentRepo loanPaymentRepo;

    @Autowired
    private KafkaProducerTransaction kafkaProducerTransaction;

    private final String serviceToken = "acsdfcsdf-asdaxcasdasd-asdasdasd";

    public ResponseDTO addMoney(String auth, TransactionDto transactionDto) throws ParseException {
//        return new ResponseDTO()
        Map<String,Object> data = extractTokenService.extractValue(auth);
        String Uid = data.get("uid").toString();

        Account account = accountRepo.findByUidAndAccountNum(Uid,transactionDto.getAccountNumber());
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDto.getAmount());
        transaction.setTransactionMethod(transactionDto.getMethod());
        transaction.setTimeStamp(LocalDateTime.now());
        double updatedBalance = account.getBalance() + transactionDto.getAmount();
        transaction.setAffected_balance(updatedBalance);
        transaction.setTransactionType("credit");
        transaction.setTransactionMethod(transactionDto.getMethod());
        if(transactionDto.getRemark() != null) {
            transaction.setRemark(transactionDto.getRemark());
        }else {
            transaction.setRemark("Self");
        }
        account.setBalance(updatedBalance);
        transaction.setAccount(account);
        var response = transactionRepo.save(transaction);
        TransactionEmail transactionEmail = new TransactionEmail();
        transactionEmail.setEmail(data.get("email").toString());
        transactionEmail.setAccount(transactionDto.getAccountNumber().toString());
        transactionEmail.setAmount(String.valueOf(response.getAmount()));
        transactionEmail.setTime(response.getTimeStamp().toString());
        transactionEmail.setMethod(response.getTransactionMethod());
        transactionEmail.setType("credit");
        transactionEmail.setAcType(account.getAccountType());
        transactionEmail.setTid(response.getTId().toString());
        transactionEmail.setRemark(response.getRemark());
        kafkaProducerTransaction.TransactionEmail(transactionEmail);

        return new ResponseDTO(StatusCodeEnum.OK.getStatusCode(),"Transaction Success",null);



    }

    public Double checkBalance(CheckBalanceDTO checkBalanceDTO) {
        Account account = accountRepo.findByUidAndAccountNum(checkBalanceDTO.getUid(),checkBalanceDTO.getAccountNum());
        return account.getBalance();
    }

    public ResponseDTO transferMoney(String auth, DebitTransactionDTO debitTransactionDTO) throws ParseException {
        Map<String,Object> data = extractTokenService.extractValue(auth);
        String Uid = data.get("uid").toString();
        Account senderAccount = accountRepo.findByUidAndAccountNum(Uid,debitTransactionDTO.getSenderAccount());
        if(senderAccount.isFreeze()){
            return new ResponseDTO(StatusCodeEnum.NOT_FOUND.getStatusCode(), "Your account is freeze please contact a bank",null);
        }
        if(!accountRepo.existsByAccountNum(debitTransactionDTO.getSenderAccount())){
            return new ResponseDTO(StatusCodeEnum.NOT_FOUND.getStatusCode(), "Account Number not valid",null);
        }
        if(!accountRepo.existsByAccountNum(debitTransactionDTO.getReviverAccount())){
            return new ResponseDTO(StatusCodeEnum.NOT_FOUND.getStatusCode(), "Receiver Account Not Found",null);
        }
//        Check Amount is greeter than transfer Amount
        if(senderAccount.getBalance() < debitTransactionDTO.getAmount()){
            return new ResponseDTO(StatusCodeEnum.NOT_FOUND.getStatusCode(), "your account balance is not sufficient",null);
        }
        Account receiverAccount = accountRepo.findByAccountNum(debitTransactionDTO.getReviverAccount());

        Transaction t1 = new Transaction();
        t1.setTransactionType("debit");
        t1.setAmount(debitTransactionDTO.getAmount());
        t1.setTransactionMethod(debitTransactionDTO.getMethod());
        double updatedBalance = senderAccount.getBalance() - debitTransactionDTO.getAmount();
        senderAccount.setBalance(updatedBalance);
        t1.setAffected_balance(updatedBalance);
        t1.setTimeStamp(LocalDateTime.now());
        t1.setAccount(senderAccount);
        t1.setRemark("transfer-"+debitTransactionDTO.getReviverAccount());
        var senderResponse = transactionRepo.save(t1);

        Transaction t2 = new Transaction();
        t2.setTransactionType("credit");
        t2.setAmount(debitTransactionDTO.getAmount());
        t2.setTransactionMethod(debitTransactionDTO.getMethod());
        t2.setTimeStamp(LocalDateTime.now());
        double updatedCreditBalance = receiverAccount.getBalance() + debitTransactionDTO.getAmount();
        receiverAccount.setBalance(updatedCreditBalance);
        t2.setAffected_balance(updatedCreditBalance);
        t2.setTimeStamp(LocalDateTime.now());
        t2.setAccount(receiverAccount);
        t2.setRemark("from-"+debitTransactionDTO.getSenderAccount());
        var receiverResponse = transactionRepo.save(t2);


        TransactionEmail transactionEmail = new TransactionEmail();
        transactionEmail.setEmail(data.get("email").toString());
        transactionEmail.setAccount(debitTransactionDTO.getSenderAccount().toString());
        transactionEmail.setAmount(String.valueOf(debitTransactionDTO.getAmount()));
        transactionEmail.setTime(senderResponse.getTimeStamp().toString());
        transactionEmail.setMethod(senderResponse.getTransactionMethod());
        transactionEmail.setAcType(senderAccount.getAccountType());
        transactionEmail.setType("debit");
        transactionEmail.setTid(senderResponse.getTId().toString());
        kafkaProducerTransaction.TransactionEmail(transactionEmail);
        transactionEmail.setEmail(receiverAccount.getEmail());
        transactionEmail.setTid(receiverResponse.getTId().toString());
        transactionEmail.setType("credit");
        transactionEmail.setAcType(receiverAccount.getAccountType());
        transactionEmail.setTime(receiverResponse.getTimeStamp().toString());
        kafkaProducerTransaction.TransactionEmail(transactionEmail);

        return new ResponseDTO(StatusCodeEnum.OK.getStatusCode(), "Transaction Success",null);

    }

    public Map<Long,Double> listOfBalanceByPID(List<Long> list) {
        try {
            List<LoanPayment> lp = loanPaymentRepo.findAllByLpIdIn(list);

            List<Long> accountNum = lp.stream()
                    .map(LoanPayment::getAccountNum)
                    .collect(Collectors.toList());

            List<Account> ac = accountRepo.findAllById(accountNum);

            Map<Long, Double> balance = ac.stream().collect(Collectors.toMap(
                    Account::getAccountNum,
                    Account::getBalance
            ));

            Map<Long, Double> response = lp.stream().collect(Collectors.toMap(
                    LoanPayment::getLpId,
                    bal -> balance.get(bal.getAccountNum())
            ));
            return response;
        }catch (Exception e){
            System.err.println(e.getMessage());
            return Map.of(0L,0.0D);
        }
    }

    public Object makeFDTransaction(String authorization, FdBookTransaction transactionDTO) throws ParseException {

        if(!serviceToken.equals(transactionDTO.getSecret())){
            return new ResponseDTO("400","Invalid Request",null);
        }
        Map<String,Object> data = extractTokenService.extractValue(authorization);
        String Uid = data.get("uid").toString();

        Account account = accountRepo.findByUidAndAccountNum(Uid, transactionDTO.getAccount());
        Transaction transaction = new Transaction();


        transaction.setAmount(transactionDTO.getAmount());
        transaction.setTransactionMethod("FD");
        transaction.setTimeStamp(LocalDateTime.now());
        double updatedBalance = account.getBalance() - transactionDTO.getAmount();
//        System.err.println("updated balance :: "+updatedBalance);
        transaction.setAffected_balance(updatedBalance);
        transaction.setTransactionType("debit");
        transaction.setTransactionMethod("FD");
        transaction.setRemark("FD-"+transactionDTO.getFdID());
        account.setBalance(updatedBalance);
        transaction.setAccount(account);
        var response = transactionRepo.save(transaction);
        TransactionEmail transactionEmail = new TransactionEmail();
        transactionEmail.setEmail(data.get("email").toString());
        transactionEmail.setAccount(account.getAccountNum().toString());
        transactionEmail.setAmount(String.valueOf(response.getAmount()));
        transactionEmail.setTime(response.getTimeStamp().toString());
        transactionEmail.setMethod(response.getTransactionMethod());
        transactionEmail.setType("debit");
        transactionEmail.setTid(response.getTId().toString());
        kafkaProducerTransaction.TransactionEmail(transactionEmail);

        return updatedBalance;
    }
}
