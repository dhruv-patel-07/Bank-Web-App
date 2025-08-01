package com.bank.web.app.account.service;

import com.bank.web.app.account.dto.*;
import com.bank.web.app.account.model.AccountDetails;
import com.bank.web.app.account.model.FD;
import com.bank.web.app.account.repo.AccountDetailsRepo;
import com.bank.web.app.account.repo.FdRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class FDAccountService {

    @Autowired
    private AccountDetailsRepo accountDetailsRepo;

    @Autowired
    private ExtractTokenService extractTokenService;

    @Autowired
    private FdRepo repo;

    @Autowired
    private FeignService feignService;

    @Autowired
    private ExternalService externalService;

    private final String serviceToken = "acsdfcsdf-asdaxcasdasd-asdasdasd";


    public ResponseDTO startFD(String auth,newFDdto fDdto) throws ParseException {
        if(fDdto.getMonth() < 6){
            return new ResponseDTO("200","month must be greater than 6",null);
        }
        AccountDetails accountDetails = accountDetailsRepo.findByAccountNumber(fDdto.getAccountNum());
        Map<String,Object> userData = extractTokenService.extractValue(auth);
        String uid = userData.get("uid").toString();

        if(!accountDetails.getAccountUser().getUid().equalsIgnoreCase(uid)){
            return new ResponseDTO("200","Account number not Valid",null);
        }
        BalanceCheck balanceCheck = new BalanceCheck();
        balanceCheck.setAccountNum(fDdto.getAccountNum());
        ResponseDTO responseDTO = externalService.balanceCheck(auth,balanceCheck);
        var data = responseDTO.object();
        System.out.println(data);
        System.err.println(data.getClass());
//        Map<String,Object> maps = (Map<String, Object>) data;
        Double bal = (Double) ((Map<?, ?>) data).get("Balance");
        System.err.println(bal);
        if(bal > fDdto.getAmount()){
            Map<String,Object> recAccount = calculateInterest(fDdto);
            FD fd = new FD();
            fd.setAccount(accountDetails);
            fd.setAmount(fDdto.getAmount());
            fd.setDuration(fDdto.getMonth());
            fd.setIsClosed(false);
            fd.setInterestRate((float) FdInterestRate(fDdto.getMonth()));
            fd.setInterestAmount((Double)recAccount.get("interest"));
            fd.setStartDate(LocalDate.now());
            fd.setMaturityDate(LocalDate.now().plusMonths(fDdto.getMonth()));
            fd.setMaturityAmount((Double) recAccount.get("maturityAmount"));
            log.info("Maturity amount :: {}",(Double) recAccount.get("maturityAmount"));
            var response = repo.save(fd);
            Double balance = MakeFDTransaction(auth,response);
            log.info("Balance :: {}",balance);
            Map<String,Double> map = Map.of("balance", balance);
            return new ResponseDTO("200","FD book success",map);
        }
        else {
            return new ResponseDTO("200","Balance is not enough",null);
        }

    }
    public Double MakeFDTransaction(String auth,FD fd){
        FdBookTransaction transaction = new FdBookTransaction();
        transaction.setAccount(fd.getAccount().getAccountNumber());
        transaction.setAmount(fd.getAmount());
        transaction.setSecret(serviceToken);
        transaction.setFdID(fd.getId());
        return (Double) feignService.FDTransaction(auth,transaction);

    }

    public Map<String,Object> calculateInterest(newFDdto data) {
        int months = data.getMonth();
        double amount = data.getAmount();
        double R = FdInterestRate(months);
        double timeInYears = months / 12.0;
        double interest = (amount * R * timeInYears) / 100;
        double maturityAmount = amount + interest;

        Map<String,Object> map = new HashMap<>();
        map.put("month",months);
        map.put("interest",interest);
        map.put("FdAmount",amount);
        map.put("maturityAmount",maturityAmount);
        return map;
    }

    public double FdInterestRate(int months){
        double interest = 0;
        if(months < 6){
            interest = 4.5;
        } else if (months >= 6 && months < 9) {
            interest = 5.75;
        }
        else if (months >= 9 && months < 12) {
            interest = 6.70;
        }
        else if(months >= 12 && months< 15){
            interest = 7.40;
        }
        else if(months >= 18 && months< 24){
            interest = 7.50;
        } else if (months >= 24 && months < 60) {
            interest = 7.20;

        } else if (months >= 60) {
            interest = 7.10;
        }
        return interest;
    }

    public ResponseDTO withDrawFd(String auth,FdWithdraw drawFd) throws ParseException {
        Optional<FD> fd = repo.findById(drawFd.getFdNum());
        if(fd.isEmpty()){
            return new ResponseDTO("404","FD Not Found",null);
        }
        Map<String,Object> userData = extractTokenService.extractValue(auth);
        String uid = userData.get("uid").toString();
        if(fd.get().getAccount().getAccountUser().getUid().equals(uid)){
            Double amount;
            TransactionDto transactionDto = new TransactionDto();
            transactionDto.setAccountNumber(fd.get().getAccount().getAccountNumber());
            transactionDto.setMethod("rtgs");
            transactionDto.setRemark("FD");

            boolean isPenalty = false;
            double inte = 0.0;

            if(fd.get().getIsClosed()){
                return new ResponseDTO("200","FD already closed",null);
            }
            if(LocalDate.now().isBefore(fd.get().getMaturityDate())){
                fd.get().setWithdrawalDate(LocalDate.now());
                double timeInYears = fd.get().getDuration() / 12.0;
                float R = (float) (fd.get().getInterestRate() - 1.5);
                System.err.println("R  :: "+R);
                double interest = (fd.get().getAmount() * R * timeInYears) / 100;
                System.err.println("interest :: "+interest);
                amount = Math.round((interest+fd.get().getAmount()) * 100.00) / 100.00;

                inte = Math.round((interest+fd.get().getAmount()) * 100.00) / 100.00;
                fd.get().setWithdrawalAmount(Math.round((interest+fd.get().getAmount()) * 100.00) / 100.00);
                fd.get().setIsClosed(true);
                isPenalty = true;
                repo.save(fd.get());
                transactionDto.setAmount(amount);
            }else {
                fd.get().setWithdrawalDate(LocalDate.now());
                fd.get().setWithdrawalAmount(fd.get().getMaturityAmount());
                fd.get().setIsClosed(true);
                inte = fd.get().getMaturityAmount();
                repo.save(fd.get());
                transactionDto.setAmount(fd.get().getMaturityAmount());
            }
            feignService.addMoney(auth,transactionDto);
            if (isPenalty) {
                Map<String,Object> map = Map.of("Amount",inte,"Penalty(%)",1.5);
                return new ResponseDTO("200", "FD Closed before maturity date", map);

            }else {
                Map<String,Object> map = Map.of("Amount",inte);
                return new ResponseDTO("200", "FD Closed Success", map);

            }


        }
        return new ResponseDTO("401", "Account Not Found", null);

    }

    public ResponseDTO getFdList(String auth,Long account) throws ParseException {
        Map<String,Object> userData = extractTokenService.extractValue(auth);
        String uid = userData.get("uid").toString();
        AccountDetails ad =accountDetailsRepo.findByAccountNumber(account);
        if(ad.getAccountUser().getUid().equalsIgnoreCase(uid)){
            List<FD> fd = ad.getFdList();
            List<FdResponse> fd1 = fd.stream().map(m->{
               FdResponse fdR = new FdResponse();
               fdR.setAmount(m.getAmount());
               fdR.setInterest(m.getInterestRate());
               fdR.setFdNum(m.getId());
               fdR.setMaturityDate(m.getMaturityDate());
               fdR.setMaturityAmount(m.getMaturityAmount());
               String status;
               if(m.getIsClosed()){
                   status = "Closed";
                   fdR.setStatus(status);
               }else {
                   status = "Active";
                   fdR.setStatus(status);
               }
               return fdR;
            }).toList();
            return new ResponseDTO("200","Fix Deposits",fd1);


        }


        return new ResponseDTO("401","Fix Deposits Not Found",null);
    }
}
//Chakrad
