package com.bank.web.schedule.Service;

import com.bank.web.schedule.Repo.AccountsRepo;
import com.bank.web.schedule.Repo.LoanPaymentRepo;
import com.bank.web.schedule.dto.ResponseDTO;
import com.bank.web.schedule.kafka.Producer.KafkaProducer;
import com.bank.web.schedule.kafka.Producer.LoanPaymentFailedDTO;
import com.bank.web.schedule.model.Accounts;
import com.bank.web.schedule.model.LoanPayment;
import com.bank.web.schedule.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScheduleService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoanPaymentRepo loanPaymentRepo;

    @Autowired
    private HelperService helperService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private FeignService feignService;

    @Autowired
    private KafkaProducer producer;

    @Autowired
    private AccountsRepo accountsRepo;


//    @Scheduled(fixedRate = 30000)
    public void EmiPayment(){
        String date = LocalDate.now().toString(); // "2025-08-08"
        List<LoanPayment> payments = loanPaymentRepo.findByScheduleDate(date);
        List<Long> Pid = payments.stream().map(p->p.getPaymentId()).collect(Collectors.toList());
//        Pid.forEach(System.out::println);
            String token;
          if(redisService.keyExists()){
              log.info("token from redis : ");
              token = redisService.getValue();
          }
          else {
              log.info("new token generate : ");
              token = helperService.ServiceAccountLogin();
          }
        if(Pid.isEmpty()){
            return;
        }


        String url = "http://GATEWAY-SERVICE/api/v1/transaction/get-list-balance";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        HttpEntity<List<Long>> request = new HttpEntity<>(Pid, headers);
        ResponseEntity<Map<Long, Double>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<Long, Double>>() {}
        );
        log.info("Data fetched :: ");
        Map<Long,Double> data = response.getBody();
        System.err.println(data);
//        Update Balance
        payments.stream()
                .filter(p -> data.containsKey(p.getPaymentId()))
                .forEach(p -> p.setBalance(data.get(p.getPaymentId())));
        loanPaymentRepo.saveAll(payments);
        log.info("i am Still working update balance");


//        updateBalance(token,payments,Pid);

        List<Long> finalPaymentListWithEnoughBalance =  payments.stream()
                .map(m->m.getPaymentId())
                .collect(Collectors.toList());

//        List<Long> finalPaymentListWithLowBalance =  payments.stream()
//                .filter(p->p.getBalance()<p.getEmi())
//                .map(m->m.getPaymentId())
//                .collect(Collectors.toList());


//        Call Transaction Service
        DeductLoanPayments(finalPaymentListWithEnoughBalance,token);
        log.info("Saving updated payments...");
        log.info("Saved.");

    }

    public void DeductLoanPayments(List<Long> PID,String token){
        String authHeader = "Bearer " + token;
        ResponseDTO response = feignService.LoanPaymentDeduct(authHeader,PID);
        PID.stream().map(m->{
            var data = loanPaymentRepo.findByPaymentId(m);
            data.setScheduleDate(LocalDate.now().plusMonths(1).toString());
            loanPaymentRepo.save(data);
            log.info("Date update");
            return m;
        });
        log.info("Loan Payment initiate :: {}",response);
    }

//    public void updateBalance(String token,List<LoanPayment> payments,List<Long> Pid){
//
//    }

//    @Scheduled(cron = "0 0 12 * * *")
    @Scheduled(fixedRate = 30000)
    public void EmiPaymentSchedule(){
        log.info("EMI Schedule start....");
        EmiPayment();
        log.info("EMI Schedule end....");

    }
    @Scheduled(cron = "0 0 14 * * *")
    public void ReminderService(){
        String date = LocalDate.now().plusDays(2).toString();
        log.info(date);
        List<LoanPayment> payments = loanPaymentRepo.findByScheduleDateAndIsReminderSendFalse(date);
        List<LoanPayment> updateDB = new ArrayList<>();
        System.out.println(payments);
        boolean isAnyChange = false;
        for(LoanPayment payment: payments){
            LoanPaymentFailedDTO loanPaymentFailedDTO = new LoanPaymentFailedDTO();
            loanPaymentFailedDTO.setLoanNum(payment.getLoanId());
            loanPaymentFailedDTO.setAmount(payment.getEmi());
            loanPaymentFailedDTO.setNextDate(LocalDate.parse(payment.getScheduleDate()));
            loanPaymentFailedDTO.setEmail(payment.getEmail());
            payment.setIsReminderSend(true);
            isAnyChange = true;
            updateDB.add(payment);
//            loanPaymentRepo.save(payment);
            producer.ReminderEmail(loanPaymentFailedDTO);
        }
        if(isAnyChange) {
            loanPaymentRepo.saveAll(updateDB);
        }
    }


}
