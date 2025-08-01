package com.bank.web.schedule.Service;

import com.bank.web.schedule.Repo.AccountsRepo;
import com.bank.web.schedule.dto.GetTransactionDTO;
import com.bank.web.schedule.dto.TransactionDataDTO;
import com.bank.web.schedule.kafka.Producer.AddInterestDTO;
import com.bank.web.schedule.kafka.Producer.KafkaProducer;
import com.bank.web.schedule.model.Accounts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class InterestService {

    @Autowired
    private FeignService feignService;

    @Autowired
    private AccountsRepo accountsRepo;

    @Autowired
    private HelperService helperService;

    @Autowired
    private KafkaProducer producer;

    String currentYear = String.valueOf(LocalDate.now().getYear());

    @Scheduled(cron = "0 0 0 1 1 *")
    public void Q1() {
        log.info("Q1 interest out");
        interestService("01-01-"+currentYear,"31-03-"+currentYear);
    }

    @Scheduled(cron = "0 0 0 1 4 *")
    public void Q2() {
        log.info("Q2 interest out");

        interestService("01-04-"+currentYear,"31-06-"+currentYear);
    }

    @Scheduled(cron = "0 0 0 1 7 *")
    public void Q3() {
        log.info("Q3 interest out");

        interestService("01-07-"+currentYear,"30-09-"+currentYear);
    }

    @Scheduled(cron = "0 0 0 1 10 *")
    public void Q4() {
        log.info("Q4 interest out");
        interestService("01-10-"+currentYear,"31-12-"+currentYear);
    }

    public void interestService(String start,String end){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS");
        LocalDateTime dt1 = LocalDateTime.parse(start + " 00:00:00.000", formatter);
        LocalDateTime dt2 = LocalDateTime.parse(end + " 23:59:59.000", formatter);
        List<Accounts> accountList = accountsRepo.findAll();
        List<LocalDateTime> daysList = getInterestRateDate();
        for (Accounts accounts : accountList) {
            if(accounts.getType().equalsIgnoreCase("CURRENT")){
                log.info("Skipped current account");
                continue;
            }
            System.out.println("Saving account");
            System.out.println(accounts.getAccountNum());
            GetTransactionDTO td = new GetTransactionDTO();

            td.setStartTime(dt1);
            td.setEndTime(dt2);
            td.setAccountNum(accounts.getAccountNum());
            String token = "Bearer " + helperService.ServiceAccountLogin();
            List<TransactionDataDTO> transaction = feignService.getTransactionDetails(token, td);
            log.info("Transactions :: {}", transaction);
            System.out.println(daysList);
            List<Double> amountPerDay = new ArrayList<>();
            List<Double> dailyInterest = new ArrayList<>();

            for (LocalDateTime date : daysList) {
                long closingId = transaction.stream()
                        .filter(tx -> tx.getTimeStamp().toLocalDate().equals(date.toLocalDate()))
                        .mapToLong(TransactionDataDTO::getTId)
                        .max()
                        .orElse(0L);
//                System.err.println("Closing id :: "+closingId);
                double interestRate = transaction.stream()
                        .filter(tx -> tx.getTId() != 0L && tx.getTId().equals(closingId))
                        .mapToDouble(TransactionDataDTO::getAffected_balance) // returns DoubleStream
                        .findAny()
                        .orElse(0.0);
//                System.err.println("Closing balance :: "+interestRate);


                double rate = 3.0; // Annual interest rate in percent
                double dayInter = (interestRate * rate)/(365*100);
//                Daily Interest Rate
                dailyInterest.add(dayInter);
//                amountPerDay.add(dailyTotalCredit);
            }
            double TotalInterest = dailyInterest
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .sum();

            log.info("Total interest :: {}",TotalInterest);
            log.info("Amount per day :: {}",amountPerDay);
            log.info("Daily Interest rate :: {}",dailyInterest);

//            Kafka
            AddInterestDTO addInterestDTO = new AddInterestDTO();
            addInterestDTO.setRemark("interest");
            addInterestDTO.setAmount(TotalInterest);
            addInterestDTO.setAccountNumber(accounts.getAccountNum());
            addInterestDTO.setMethod("neft");
            if(TotalInterest != 0.0d) {
                producer.InterestRateService(addInterestDTO);
            }


        }
    }

    public List<LocalDateTime> getInterestRateDate(){
        List<LocalDateTime> daysList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dt1 = LocalDateTime.parse("2025-07-01" + " 00:00:00.000",formatter);
        LocalDateTime dt2 = LocalDateTime.parse("2025-07-15" + " 00:00:00.000",formatter);

        while (!dt1.isAfter(dt2)) {
            daysList.add(dt1);
            dt1 = dt1.plusDays(1);
        }
        return daysList;
    }
}
