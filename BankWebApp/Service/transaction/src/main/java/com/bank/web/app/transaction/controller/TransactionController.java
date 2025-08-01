package com.bank.web.app.transaction.controller;

import com.bank.web.app.transaction.Elasticsearch.SearchService;
import com.bank.web.app.transaction.Service.*;
import com.bank.web.app.transaction.dto.*;
import com.bank.web.app.transaction.enums.StatusCodeEnum;
import com.bank.web.app.transaction.kafka.RecurringPayment;
import com.bank.web.app.transaction.model.Transaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import org.aspectj.lang.annotation.DeclareWarning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transaction/")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private RecurringService recurringService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private SearchService searchService;

    @PostMapping("deposit")
    public ResponseDTO addMoney(@RequestHeader("Authorization") String auth, @Valid @RequestBody TransactionDto transactionDto, Errors errors) throws ParseException {
        if(errors.hasErrors()){
            return new ResponseDTO(StatusCodeEnum.ERROR.getStatusCode(), errors.getAllErrors().get(0).getDefaultMessage(),null);
        }
        return transactionService.addMoney(auth,transactionDto);
    }

    @PostMapping("transfer")
    public ResponseDTO transferMoney(@RequestHeader("Authorization") String auth, @Valid @RequestBody DebitTransactionDTO debitTransactionDTO, Errors errors) throws ParseException {
        if(errors.hasErrors()){
            return new ResponseDTO(StatusCodeEnum.ERROR.getStatusCode(), errors.getAllErrors().get(0).getDefaultMessage(),null);
        }
        return transactionService.transferMoney(auth,debitTransactionDTO);
    }

    @PostMapping("check-balance")
    public Double CheckBalance(@RequestBody CheckBalanceDTO checkBalanceDTO){
        return transactionService.checkBalance(checkBalanceDTO);

    }

    @PostMapping("get-list-balance")
    public Map<Long,Double> listBalance(@RequestBody List<Long> list){
        return transactionService.listOfBalanceByPID(list);
    }

    @PostMapping("emi-deduct")
    public void deductEmi(@RequestBody List<Long> PidList){
        loanService.deductEmi(PidList);

    }

    @GetMapping("download-transaction-pdf/{account}/{month}")
    public ResponseEntity<?> MonthlyTransactionEmail(@RequestHeader("Authorization") String authorization,@PathVariable Long account, @PathVariable int month){

        ByteArrayInputStream pdf = pdfService.SendPdfData(authorization,account,month);
        HttpHeaders  httpHeaders  = new HttpHeaders();
        httpHeaders.add("Content-Disposition","inline;file=statement.pdf");
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));

    }

    @PostMapping("transaction-details")
    public List<TransactionDataDTO> getTransactionDetails(@RequestHeader("Authorization") String authorization,@RequestBody GetTransactionDTO transactionDTO) throws JsonProcessingException {
        return pdfService.SendPdfData1(transactionDTO);
    }

    @PostMapping("recurring-payments")
    public ResponseDTO recurringPayment(@RequestHeader("Authorization") String authorization, @RequestBody TransactionDto transactionDTO) throws JsonProcessingException, ParseException {
        return recurringService.savePayment(authorization,transactionDTO);
    }

//    @PostMapping("add-interest")
//    public ResponseDTO addInterest(@RequestHeader("Authorization") String auth, @Valid @RequestBody TransactionDto transactionDto, Errors errors) throws ParseException
//        {
//            return sa
//        }

    @PutMapping("book-fd")
    public Object FDTransaction(@RequestHeader("Authorization") String authorization, @RequestBody FdBookTransaction fdBookTransaction) throws JsonProcessingException, ParseException {
        return transactionService.makeFDTransaction(authorization,fdBookTransaction);
    }

    @PutMapping("admin/freeze")
    private ResponseDTO freezeAccount(@RequestBody FreezeAccount freezeAccount){
        return adminService.freezeAccount(freezeAccount);
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "reportBalanceFallBack")
    @GetMapping("admin/search/{keyword}")
    private ResponseDTO search(@PathVariable String keyword) throws Exception {
        return searchService.searchByKeyword(keyword);
    }
    private ResponseDTO reportBalanceFallBack(@PathVariable String keyword,Throwable tx){
        return new ResponseDTO("503","Service Unavailable",null);
    }

    @GetMapping("employee/branch-report")
    private ResponseDTO searchBranch(@RequestHeader("Authorization") String authHeader,@RequestParam(required = false) Integer weeks,@RequestParam(required = false) Integer month,@RequestParam(required = false) String start,@RequestParam(required = false) String end) throws Exception {
        return searchService.viewReport(authHeader,weeks,month,start,end);
    }

    @GetMapping("admin/branch-report/{branch}")
    private ResponseDTO searchBranch(@RequestHeader("Authorization") String authHeader,@PathVariable String branch,@RequestParam(required = false) Integer weeks,@RequestParam(required = false) Integer month,@RequestParam(required = false) String start,@RequestParam(required = false) String end) throws Exception {
        return searchService.viewReportAdmin(authHeader,branch,weeks,month,start,end);
    }

    @GetMapping("heartbeat")
    public ResponseEntity<String> heartbeat() {
        return ResponseEntity.ok("ALIVE");
    }
}
