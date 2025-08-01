package com.bank.web.schedule.Service;

import com.bank.web.schedule.Repo.AccountsRepo;
import com.bank.web.schedule.dto.GetTransactionDTO;
import com.bank.web.schedule.dto.TransactionDataDTO;
import com.bank.web.schedule.model.Accounts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;


@Service
@Slf4j
public class StatementEmailService {

    @Autowired
    private AccountsRepo accountsRepo;

    @Autowired
    private FeignService feignService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private EmailStatementService emailStatementService;


    @Scheduled(cron = "0 0 9 1 * ?")
    public void StatementGroup1(){
        log.info("Group 1 is activate...");
        List<Accounts> accounts = getPercentRange(0.0,25.0);
        StatementEmail(accounts);

    }

    @Scheduled(cron = "0 0 10 1 * ?")
    public void StatementGroup2(){
        log.info("Group 2 is activate...");
        List<Accounts> accounts = getPercentRange(26.0,50.0);
        StatementEmail(accounts);


    }

    @Scheduled(cron = "0 16 11 1 * ?")
    public void StatementGroup3(){
        log.info("Group 3 is activate...");
        System.out.println("Calling Group 3");
        List<Accounts> accounts = getPercentRange(51.0,75.0);
        StatementEmail(accounts);


    }

    @Scheduled(cron = "0 0 12 1 * ?")
    public void StatementGroup4(){
        log.info("Group 4 is activate...");

        List<Accounts> accounts = getPercentRange(76.0,100);
        StatementEmail(accounts);

    }

    public void StatementEmail(List<Accounts> accounts){
//        LocalDate firstDayOfLastMonth = YearMonth.now().minusMonths(1).atDay(1);
//        LocalDateTime startDateTime = firstDayOfLastMonth.atStartOfDay();
//        LocalDate lastDayOfLastMonth = YearMonth.now().minusMonths(1).atEndOfMonth();
//        LocalDateTime endDateTime = lastDayOfLastMonth.atTime(23, 59, 59);

        LocalDateTime startDateTime = LocalDateTime.now().minusDays(10);
        LocalDateTime endDateTime = LocalDateTime.now();
        System.out.println(helperService.ServiceAccountLogin());
        LocalDate sDate = startDateTime.toLocalDate();
        LocalDate eDate = endDateTime.toLocalDate();
        String month = LocalDate.now()
                .minusMonths(1)
                .getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                .toLowerCase();

        for(Accounts ac: accounts){
            log.info("Accounts :: {}",ac);
            GetTransactionDTO transactionDTO = new GetTransactionDTO();
            transactionDTO.setAccountNum(ac.getAccountNum());
            transactionDTO.setStartTime(startDateTime);
            transactionDTO.setEndTime(endDateTime);
            String auth = helperService.ServiceAccountLogin();
            String token = "Bearer " + auth;
            List<TransactionDataDTO> t = feignService.getTransactionDetails(token,transactionDTO);

//            for (TransactionDataDTO t1: t){
            emailStatementService.EmailStatement(token,ac.getAccountNum(),t,month,sDate.toString(),eDate.toString());
//            }
        }
    }

    //    for find StatementValue form 0-25 like this
    @Async
    public List<Accounts> getPercentRange(double fromPercent, double toPercent) {
        if (fromPercent < 0 || toPercent > 100 || fromPercent >= toPercent) {
            throw new IllegalArgumentException("Invalid percentage range");
        }

        long total = accountsRepo.count();

        int from = (int) Math.floor(total * (fromPercent / 100));
        int to = (int) Math.floor(total * (toPercent / 100));

        int size = to - from;
        int page = from / size; // Calculate page index for PageRequest

        PageRequest pageRequest = PageRequest.of(page, size);
        return accountsRepo.findAll(pageRequest).getContent();
    }
}
