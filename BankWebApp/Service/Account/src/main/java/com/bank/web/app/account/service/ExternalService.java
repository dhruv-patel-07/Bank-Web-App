package com.bank.web.app.account.service;

import com.bank.web.app.account.dto.BalanceCheck;
import com.bank.web.app.account.dto.ResponseDTO;
import com.bank.web.app.account.dto.UserDisplayDTO;
import com.bank.web.app.account.dto.UserInfoDTO;
import com.bank.web.app.account.model.AccountDetails;
import com.bank.web.app.account.repo.AccountDetailsRepo;
import com.bank.web.app.account.repo.AccountUserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Service
@Slf4j
public class ExternalService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExtractTokenService extractTokenService;

    @Autowired
    private AccountDetailsRepo accountDetailsRepo;

    public ResponseDTO balanceCheck(String token,BalanceCheck balanceCheck) throws ParseException {

        Map<String,Object> map = extractTokenService.extractValue(token);
        String uid = map.get("uid").toString();


        HttpHeaders headers =  new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> requestBody = Map.of(
                "accountNum", balanceCheck.getAccountNum(),
                "uid", uid
        );

        String url = "http://TRANSACTION-SERVICE/api/v1/transaction/check-balance";
        HttpEntity<Map<String,Object>> reHttpEntity = new HttpEntity<>(requestBody,headers);
        ResponseEntity<Double> response = restTemplate.exchange(url, HttpMethod.POST,reHttpEntity, Double.class);
//        System.err.println(response.getBody());
        AccountDetails accountDetails = accountDetailsRepo.findByAccountNumber(balanceCheck.getAccountNum());
        if(!Objects.equals(accountDetails.getBalance(), response.getBody())){
            accountDetails.setBalance(response.getBody());
            accountDetailsRepo.save(accountDetails);
            log.info("Balance updated!");
        }
        Map<String,Object> obj = new HashMap<>();
        obj.put("Balance",response.getBody());
        obj.put("Account",balanceCheck.getAccountNum());
        return new ResponseDTO("200","Balance fetched!",obj);
    }

    public UserInfoDTO getUserDetails(Long account) {
        AccountDetails accountDetails = accountDetailsRepo.findByAccountNumber(account);
        if(accountDetails == null){
            return null;
        }


        UserInfoDTO obj = new UserInfoDTO();
        obj.setName(accountDetails.getAccountUser().getName());
        obj.setAccountNum(accountDetails.getAccountNumber().toString());
        obj.setType(accountDetails.getAccountType());
        obj.setBranch(accountDetails.getBranch().getBranchName());
        obj.setUid(accountDetails.getAccountUser().getUid());
        obj.setEmail(accountDetails.getAccountUser().getEmail());
        log.info("Data :: {}",obj);
        return obj;

    }
}
