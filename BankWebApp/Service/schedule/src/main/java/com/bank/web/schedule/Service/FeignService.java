package com.bank.web.schedule.Service;

import com.bank.web.schedule.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@FeignClient(name = "GATEWAY-SERVICE")
public interface FeignService {

    @PostMapping("/api/v1/transaction/emi-deduct")
    ResponseDTO LoanPaymentDeduct(@RequestHeader("Authorization") String authorization, List<Long> PaymentID);

    @GetMapping("/api/v1/account/user/get-account-details/{account}")
    UserInfoDTO getAccountDetails(@RequestHeader("Authorization") String authorization, @PathVariable Long account);

    @PostMapping("/api/v1/transaction/transaction-details")
    List<TransactionDataDTO> getTransactionDetails(@RequestHeader("Authorization") String authorization, @RequestBody GetTransactionDTO transactionDTO);
}

