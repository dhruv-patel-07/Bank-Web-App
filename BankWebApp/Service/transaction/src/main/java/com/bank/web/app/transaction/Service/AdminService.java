package com.bank.web.app.transaction.Service;

import com.bank.web.app.transaction.Repo.AccountRepo;
import com.bank.web.app.transaction.dto.FreezeAccount;
import com.bank.web.app.transaction.dto.ResponseDTO;
import com.bank.web.app.transaction.kafka.producer.KafkaProducerTransaction;
import com.bank.web.app.transaction.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private KafkaProducerTransaction kafkaProducerTransaction;

    public ResponseDTO freezeAccount(FreezeAccount freezeAccount) {
        if(!accountRepo.existsByAccountNum(freezeAccount.getAccount())){
            return new ResponseDTO("404","Account not found with this account number",null);
        }

        Account account1 = accountRepo.findByAccountNum(freezeAccount.getAccount());
        if(account1.isFreeze() && freezeAccount.isFreeze()){
            return new ResponseDTO("404","Account already Frozen",null);
        }
        if(!account1.isFreeze() && freezeAccount.isFreeze()){
            return new ResponseDTO("404","Account already active",null);
        }
        account1.setFreeze(freezeAccount.isFreeze());
        account1.setDescription(freezeAccount.getDesc());
        accountRepo.save(account1);
        Map<String,Object> map = new HashMap<>();
        map.put("account",freezeAccount.getAccount());
        map.put("freeze",freezeAccount.isFreeze());
        map.put("desc",freezeAccount.getDesc());
        FreezeAccount freezeAccountEmail = new FreezeAccount();
        freezeAccountEmail.setAccount(account1.getAccountNum());
        freezeAccountEmail.setDesc(freezeAccount.getDesc());
        freezeAccountEmail.setEmail(account1.getEmail());
        freezeAccountEmail.setFreeze(freezeAccount.isFreeze());
        kafkaProducerTransaction.AccountFreeze(freezeAccountEmail);

        return new ResponseDTO("200","Request success",map);
    }
}
