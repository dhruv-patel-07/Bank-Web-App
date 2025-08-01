package com.bank.web.app.account.controller;

import com.bank.web.app.account.dto.*;
import com.bank.web.app.account.service.*;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.text.ParseException;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/account/user")
public class AccountUserController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private BranchService branchService;
    @Autowired
    private ExternalService externalService;

    @Autowired
    private InterstRateService interstRateService;

    @Autowired
    private AccountManageService accountManageService;

    @Autowired
    private LoanAccountService loanAccountService;

    @Autowired
    private FDAccountService fdAccountService;

    @Autowired
    private RecurringAccountService recurringAccountService;

//    @GetMapping("/swagger-ui")
//    public String redirectToSwaggerUI() {
//        return "redirect:/swagger-ui/index.html";
//    }

    @PostMapping("/create-account")
    public ResponseDTO createAccounts(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody AccountUserDto accountUserDto, Errors error) throws ParseException {
        if (error.hasErrors()) {
            return new ResponseDTO("400", error.getAllErrors().get(0).getDefaultMessage(), null);
        }
        return accountService.createAccount(authHeader, accountUserDto);
    }


    @PostMapping("/add-branch")
    public ResponseDTO addBranch(@Valid @RequestBody BranchDTO branchDTO, Errors errors) {
        if (errors.hasErrors()) {
            return new ResponseDTO("400", errors.getAllErrors().get(0).getDefaultMessage(), null);
        }
        return branchService.addNewBranch(branchDTO);
    }

//    public ResponseDTO addEmployee(@RequestBody EmployeeDTO employeeDTO){
//
//    }

    @GetMapping("/pending-active-account")
    public ResponseDTO NotActiveAccount(@RequestHeader("Authorization") String Auth) throws ParseException {
        return accountManageService.notActiveAccount(Auth);
    }


    @PostMapping("/add-employee")
    public ResponseDTO addEmployee(@RequestHeader("Authorization") String auth, @RequestBody EmployeeDTO employeeDTO) throws ParseException {
        return accountService.addEmployee(auth, employeeDTO);
    }

    @PutMapping("/active-account/{uid}")
    public ResponseDTO activateAccount(@PathVariable String uid) throws ParseException {
        return accountManageService.activateAccount(uid);
    }

    @GetMapping("/check-balance")
    @CircuitBreaker(name = "accountService", fallbackMethod = "checkBalanceFallBack")
    @RateLimiter(name = "accountRateLimiter", fallbackMethod = "checkBalanceRateLimitFallBack")
    @Bulkhead(name = "accountService", fallbackMethod = "fallback")
    public ResponseDTO creditTransactionUpdate(@RequestHeader("Authorization") String auth, @RequestBody BalanceCheck balanceCheck) throws ParseException {
        return externalService.balanceCheck(auth, balanceCheck);

    }

    public ResponseDTO checkBalanceFallBack(String auth, BalanceCheck balanceCheck, Throwable ex) {
        return new ResponseDTO("503", "Service Unavailable please try after some time", null);
    }

    public ResponseDTO checkBalanceRateLimitFallBack(String auth, BalanceCheck balanceCheck, Throwable ex) {
        return new ResponseDTO("503", "Too many requests – please try again later.", null);
    }

    public ResponseDTO fallback(String auth, BalanceCheck balanceCheck, Throwable ex) {
        return new ResponseDTO("503", "Too many concurrent requests – please try again later.", null);
    }


    @PostMapping("/create-loan-account")
    public ResponseDTO createLoanAccounts(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody NewLoanDTO newLoanDTO, Errors error) throws ParseException {
        if (error.hasErrors()) {
            return new ResponseDTO("400", error.getAllErrors().get(0).getDefaultMessage(), null);
        }
        return loanAccountService.startLoan(authHeader, newLoanDTO);
    }

    @PostMapping("/add-interst-rate")
    public ResponseDTO addLoanInterestRate(@RequestHeader("Authorization") String authHeader, @RequestBody InterestRateDTO interestRateDTO) throws ParseException {
        return interstRateService.addNew(authHeader, interestRateDTO);
    }


    @GetMapping("/get-account-details/{account}")
    public UserInfoDTO getAccountDetails(@RequestHeader("Authorization") String authorization, @PathVariable Long account) {
        return externalService.getUserDetails(account);
    }
// Recurring Account Options

    @PostMapping("/recurring-account-calculator")
    public ResponseDTO recurring(@RequestBody RecurringAccountCalculator recurringAccountCalculator) {
        log.info("Calculator call");
        return recurringAccountService.calculateInterest(recurringAccountCalculator);
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "startFdFallBack")
    @PostMapping("/start-fd")
    public ResponseDTO startFD(@RequestHeader("Authorization") String auth, @RequestBody newFDdto dto) throws ParseException {
        return fdAccountService.startFD(auth, dto);
    }
    public ResponseDTO startFdFallBack(String auth,newFDdto dto,Throwable tx)  {
        return new ResponseDTO("503","Service Unavailable",null);
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "closeFdFallBack")
    @PutMapping("/close-fd")
    public ResponseDTO closeFD(@RequestHeader("Authorization") String auth, @RequestBody FdWithdraw fdWithdraw) throws ParseException {
        return fdAccountService.withDrawFd(auth, fdWithdraw);
    }

    public ResponseDTO closeFdFallBack(String auth, FdWithdraw fdWithdraw,Throwable tx){
        log.warn("Circuit Breaker {}",tx.getMessage());
        return new ResponseDTO("503","Service Unavailable",null);
    }

    @RateLimiter(name = "accountRateLimiter", fallbackMethod = "checkFdListFallBack")
    @Bulkhead(name = "accountService", fallbackMethod = "FdListFallback")
    @GetMapping("/my-fds/{account}")
    public ResponseEntity<ResponseDTO> getFd(@RequestHeader("Authorization") String auth, @PathVariable Long account) throws ParseException {
        ResponseDTO response = fdAccountService.getFdList(auth, account);
       return ResponseEntity.ok().body(response);
    }

    public  ResponseEntity<ResponseDTO> checkFdListFallBack(String auth, Long account, Throwable ex) {
        ResponseDTO response = new ResponseDTO("503", "Too many requests – please try again later.", null);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    public  ResponseEntity<ResponseDTO> FdListFallback(String auth, Long account, Throwable ex) {
        ResponseDTO response = new ResponseDTO("503", "Too many concurrent requests – please try again later.", null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);

    }
    @GetMapping("/get-branch-name/{uid}")
    public String BranchDetails(@RequestHeader("Authorization") String auth,@PathVariable String uid){
        return branchService.findBranchNameById(uid);
    }
    @GetMapping("/heartbeat")
    public ResponseEntity<String> heartbeat() {
        log.info("Account service");
        return ResponseEntity.ok("ALIVE");
    }

}