package com.bank.web.app.transaction.Service;


import com.bank.web.app.transaction.dto.ResponseDTO;
import com.bank.web.app.transaction.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "GATEWAY-SERVICE")
public interface FeignService {

    @GetMapping("/api/v1/account/user/get-account-details/{account}")
    UserInfoDTO getAccountDetails(@RequestHeader("Authorization") String authorization, @PathVariable Long account);

    @GetMapping("/api/v1/account/user/get-branch-name/{uid}")
    String BranchDetails(@RequestHeader("Authorization") String authHeader,@PathVariable String uid);
}