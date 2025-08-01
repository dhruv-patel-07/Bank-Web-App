package com.bank.web.app.account.service;

import com.bank.web.app.account.dto.FdBookTransaction;
import com.bank.web.app.account.dto.ResponseDTO;
import com.bank.web.app.account.dto.TransactionDto;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.text.ParseException;

@FeignClient(name = "GATEWAY-SERVICE")
public interface FeignService {

    @PutMapping("/api/v1/transaction/book-fd")
    Object FDTransaction(@RequestHeader("Authorization") String authorization, @RequestBody FdBookTransaction fdBookTransaction);

    @PostMapping("/api/v1/transaction/deposit")
    ResponseDTO addMoney(@RequestHeader("Authorization") String auth, @RequestBody TransactionDto transactionDto);

}
