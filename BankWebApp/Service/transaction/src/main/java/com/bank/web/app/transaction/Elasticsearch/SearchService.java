package com.bank.web.app.transaction.Elasticsearch;


import com.bank.web.app.transaction.Repo.AccountRepo;
import com.bank.web.app.transaction.Repo.TransactionRepo;
import com.bank.web.app.transaction.Service.ExtractTokenService;
import com.bank.web.app.transaction.Service.FeignService;
import com.bank.web.app.transaction.Service.TransactionService;
import com.bank.web.app.transaction.dto.AccountSearch;
import com.bank.web.app.transaction.dto.ResponseDTO;
import com.bank.web.app.transaction.model.Account;
import com.bank.web.app.transaction.model.Transaction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class SearchService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private FeignService feignService;

    @Autowired
    private ElasticSaveService elasticSaveService;

    @Autowired
    private ExtractTokenService extractTokenService;

    @Autowired
    private TransactionRepo transactionRepo;

    public ResponseDTO searchByKeyword(String keyword) throws Exception {

//        List<Account> testAc = accountRepo.findAll();
//        testAc.stream().map(m->{
//            UserDetails userDetails = new UserDetails();
//            userDetails.setName(m.getName());
//            userDetails.setEmail(m.getEmail());
//            userDetails.setAccountNumber(m.getAccountNum());
//            userDetails.setUid(m.getUid());
//            elasticSaveService.SaveToElastic(userDetails);
//            return userDetails;
//        }).toList();

        String url = "http://localhost:9200/user/_search";
        String jsonBody = String.format("""
                {
                  "query": {
                    "multi_match": {
                      "query": "%s",
                      "fields": ["name", "email", "accountNumber"]
                    }
                  }
                }
                """, keyword);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Build request
        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
        );
        List<Long> AccountNum = extractValues(response.getBody());

        List<Account> acList = accountRepo.findAllById(AccountNum);
        List<AccountSearch> acSearch = acList.stream().map(m->{
           AccountSearch accountSearch = new AccountSearch();
           accountSearch.setAccountNum(m.getAccountNum());
           accountSearch.setEmail(m.getEmail());
           accountSearch.setActive(m.isActive());
           accountSearch.setName(m.getName());
           accountSearch.setFreeze(m.isFreeze());
           accountSearch.setBalance(m.getBalance());
           return accountSearch;
        }).toList();


        return new ResponseDTO("200","Search result",acSearch);

    }
    public List<Long> extractValues(String jsonResponse) throws Exception {
        List<Long> accountNum = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonResponse);

        JsonNode hitsArray = root.path("hits").path("hits");
        for (JsonNode hit : hitsArray) {
            JsonNode source = hit.path("_source");
//            String name = source.path("name").asText();
//            String email = source.path("email").asText();
            String account = source.path("accountNumber").asText();
            accountNum.add(Long.valueOf(account));
//            System.out.println("Name: " + name);
//            System.out.println("Email: " + email);
//            System.out.println("Account: " + account);

        }
        return accountNum;
    }

    public ResponseDTO viewReport(String authHeader, Integer weeks, Integer month, String start, String end) throws ParseException {

        Map<String,Object> tokenVal =  extractTokenService.extractValue(authHeader);
        String uid = tokenVal.get("uid").toString();
        String branch = feignService.BranchDetails(authHeader,uid);
        Map<String,Object> map = getDetails(branch,weeks,month,start,end);
        return new ResponseDTO("200","Banking details ",map);
    }

    public ResponseDTO viewReportAdmin(String authHeader, String branchName, Integer weeks, Integer month, String start, String end) {
        Map<String,Object> map = getDetails(branchName,weeks,month,start,end);
        return new ResponseDTO("200","Banking details ",map);
    }



    public Map<String,Object> getDetails(String branch,Integer weeks, Integer month, String start, String end){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        List<Transaction> transactions = List.of();
        if(weeks != null && month == null && start== null && end== null){
            LocalDate weekStart = LocalDate.now().minusWeeks(weeks);
            LocalDate weekEnd = LocalDate.now();
            LocalDateTime WS =LocalDateTime.parse(weekStart + " 00:00:00.000",formatter);
            LocalDateTime WE =LocalDateTime.parse(weekEnd + " 23:59:59.999",formatter);
            transactions = transactionRepo.findByAccount_BranchNameAndTimeStampBetween(branch,WS,WE,Sort.by(Sort.Direction.DESC,"timeStamp"));
        }
        else if(month != null && start== null && end== null && weeks==null){
            LocalDate monthStart = LocalDate.now().minusMonths(month);
            LocalDateTime MS =LocalDateTime.parse(monthStart + " 00:00:00.000",formatter);
            LocalDateTime ME = LocalDateTime.now();
            transactions = transactionRepo.findByAccount_BranchNameAndTimeStampBetween(branch,MS,ME,Sort.by(Sort.Direction.DESC,"timeStamp"));
        }
        else if (start!=null && end!=null) {
            LocalDateTime dt1 = LocalDateTime.parse(start + " 00:00:00.000",formatter);
            LocalDateTime dt2 = LocalDateTime.parse(end + " 23:59:59.999",formatter);
            transactions = transactionRepo.findByAccount_BranchNameAndTimeStampBetween(branch,dt1,dt2,Sort.by(Sort.Direction.DESC,"timeStamp"));
        }
        else {
            return null;
        }
        double TotalDeposit = transactions.stream()
                .filter(f->f.getTransactionType().equalsIgnoreCase("credit"))
                .mapToDouble(Transaction::getAmount)
                .sum();
        double TotalWithdrawal = transactions.stream()
                .filter(f->f.getTransactionType().equalsIgnoreCase("debit"))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double DepositCount = transactions.stream()
                .filter(f->f.getTransactionType().equalsIgnoreCase("credit"))
                .mapToDouble(Transaction::getAmount)
                .count();
        double WithdrawalCount = transactions.stream()
                .filter(f->f.getTransactionType().equalsIgnoreCase("debit"))
                .mapToDouble(Transaction::getAmount)
                .count();
        Map<String,Object> map = new HashMap<>();
        map.put("Deposits",Math.floor(TotalDeposit * 100)/100);
        map.put("Transactions",transactions.size());
        map.put("DepositsTransactions",DepositCount);
        map.put("WithdrawalTransactions",WithdrawalCount);
        map.put("Withdrawal",Math.floor(TotalWithdrawal *100)/100);
        map.put("Branch",branch);
//        Total deposit
//        System.out.println(TotalDeposit);
        return map;


    }
}
