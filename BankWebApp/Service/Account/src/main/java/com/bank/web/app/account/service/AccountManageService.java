package com.bank.web.app.account.service;

import com.bank.web.app.account.dto.AccountDetailsDto;
import com.bank.web.app.account.dto.BalanceCheck;
import com.bank.web.app.account.dto.ResponseDTO;
import com.bank.web.app.account.dto.UserDisplayDTO;
import com.bank.web.app.account.kafka.AccountKafkaDTO;
import com.bank.web.app.account.kafka.Producer;
import com.bank.web.app.account.kafka.RecurringPayment;
import com.bank.web.app.account.model.AccountDetails;
import com.bank.web.app.account.model.AccountUser;
import com.bank.web.app.account.model.RecurringAccount;
import com.bank.web.app.account.repo.AccountDetailsRepo;
import com.bank.web.app.account.repo.AccountUserRepo;
import com.bank.web.app.account.repo.RecurringAccountRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AccountManageService {

    @Autowired
    private AccountDetailsRepo accountDetailsRepo;

    @Autowired
    private ExtractTokenService extractTokenService;

    @Autowired
    private AccountUserRepo accountUserRepo;


    @Autowired
    private RecurringAccountRepo recurringAccountRepo;

    @Autowired
    private Producer producer;


    public ResponseDTO notActiveAccount(String authHeader) throws ParseException {
        Map<String,Object> map =  extractTokenService.extractValue(authHeader);
        String uid = map.get("uid").toString();
        log.info("UID {}",uid);
        var data = accountDetailsRepo.findAccountByBranchId(accountUserRepo.findByUid(uid).getAccountDetails().get(0).getBranch().getBranchId());
        List<UserDisplayDTO> list = data.stream()
                .map(m->{
                    UserDisplayDTO userDisplayDTO = new UserDisplayDTO();
                    userDisplayDTO.setAccount(m.getAccountNumber().toString());
                    userDisplayDTO.setEmail(m.getAccountUser().getEmail());
                    userDisplayDTO.setUID(m.getAccountUser().getUid());
                    userDisplayDTO.setAadharCard(m.getAccountUser().getAadharNumber());
                    userDisplayDTO.setPanCard(m.getAccountUser().getPanCardNumber());
                    userDisplayDTO.setType(m.getAccountType());
                            return userDisplayDTO;
                }).toList();
        return new ResponseDTO("200","Pending approval accounts",list);
    }

    public ResponseDTO activateAccount(String accountNum) {
        if(accountDetailsRepo.existsById(Long.parseLong(accountNum))){
            AccountDetails accountDetails = accountDetailsRepo.findByAccountNumber(Long.valueOf(accountNum));
            if(accountDetails.getIsActive()){
                return new ResponseDTO("200","account already active :: "+accountNum,null);
            }
            accountDetails.setIsActive(true);
            var data = accountDetailsRepo.save(accountDetails);
            AccountKafkaDTO accountKafkaDTO = new AccountKafkaDTO();
            accountKafkaDTO.setAccountNum(data.getAccountNumber());
            accountKafkaDTO.setEmail(data.getAccountUser().getEmail());
            accountKafkaDTO.setBalance(data.getBalance());
            accountKafkaDTO.setBranchId(data.getBranch().getBranchCode());
            accountKafkaDTO.setBranchName(data.getBranch().getBranchName());
            accountKafkaDTO.setAccountType(data.getAccountType());
            accountKafkaDTO.setUid(data.getAccountUser().getUid());
            accountKafkaDTO.setName(data.getAccountUser().getName());
            producer.sendAccountDetails(accountKafkaDTO);
            producer.sendAccountDetailsToTransaction(accountKafkaDTO);

            if(accountDetails.getAccountType().equalsIgnoreCase("recurring")){
                var reAc = recurringAccountRepo.findByAccountNum(Long.parseLong(accountNum));
                RecurringPayment recurringPayment = new RecurringPayment();
                recurringPayment.setAccountNum(Long.parseLong(accountNum));
                recurringPayment.setScheduleDate(reAc.getStartDate());
//                recurringPayment.setPaymentAmount(reAc.getSipAmount());
                recurringPayment.setInterestRate(reAc.getInterestRate());
                recurringPayment.setSipAmount(reAc.getSipAmount());
                recurringPayment.setMonth(reAc.getMonth());
                recurringPayment.setDueAmount(reAc.getSipAmount());
                recurringPayment.setRacId(reAc.getId());
                recurringPayment.setPaidAmount(0.0);
                recurringPayment.setEmail(accountDetails.getAccountUser().getEmail());
                recurringPayment.setTotalAmount(reAc.getTotalAmount());
                producer.sendRecurringAccountDetails(recurringPayment);
            }

            return new ResponseDTO("200","account activate :: "+accountNum,null);
        }
        return new ResponseDTO("404","account not found with Account Number :: "+accountNum,null);

    }

}
