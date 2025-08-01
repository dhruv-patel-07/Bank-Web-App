package com.bank.web.app.download.service;


import com.bank.web.app.download.dto.GetTransactionDTO;
import com.bank.web.app.download.dto.TransactionDataDTO;
import com.bank.web.app.download.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "GATEWAY-SERVICE")
public interface FeignService {


    @GetMapping("/api/v1/account/user/get-account-details/{account}")
    UserInfoDTO getAccountDetails(@RequestHeader("Authorization") String authorization, @PathVariable Long account);

    @PostMapping("/api/v1/transaction/transaction-details")
    List<TransactionDataDTO> getTransactionDetails(@RequestHeader("Authorization") String authorization, @RequestBody GetTransactionDTO transactionDTO);
}
