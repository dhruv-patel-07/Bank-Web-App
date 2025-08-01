package com.bank.web.app.account.service;

import com.bank.web.app.account.dto.AccountUserDto;
import com.bank.web.app.account.dto.NewLoanDTO;
import com.bank.web.app.account.dto.ResponseDTO;
import com.bank.web.app.account.kafka.LoanPaymentDTO;
import com.bank.web.app.account.kafka.Producer;
import com.bank.web.app.account.model.AccountDetails;
import com.bank.web.app.account.model.Interest;
import com.bank.web.app.account.model.Loan;
import com.bank.web.app.account.repo.AccountDetailsRepo;
import com.bank.web.app.account.repo.LoanRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class LoanAccountService {

    @Autowired
    private ExtractTokenService extractTokenService;

    @Autowired
    private AccountDetailsRepo accountDetailsRepo;

    @Autowired
    private LoanRepo loanRepo;

    @Autowired
    private Producer producer;

    public ResponseDTO startLoan(String authHeader, NewLoanDTO newLoanDTO) throws ParseException {
        Map<String,Object> map = extractTokenService.extractValue(authHeader);
        String uid =  map.get("uid").toString();
        AccountDetails accountDetails  = accountDetailsRepo.findByAccountNumber(newLoanDTO.getAccountNum());
        if(!accountDetails.getIsActive()){
            return new ResponseDTO("200","Account is not active",null);
        }
//
        Map<String,String> interestAmt  = new HashMap<>();
        Map<String,String> oneYearIncrement  = new HashMap<>();
        var interestRate = accountDetails.getBranch().getInterestRates();
        for(Interest interest :interestRate){
            interestAmt.put(interest.getLoanType(),String.valueOf(interest.getFixedRate()));
            oneYearIncrement.put(interest.getLoanType(),String.valueOf(interest.getOneYearIncrement()));
        }
        String get_loan_type = interestAmt.get(newLoanDTO.getLoanType().toLowerCase());
        String get_increment = oneYearIncrement.get(newLoanDTO.getLoanType().toLowerCase());
        float year = (Math.round(newLoanDTO.getDuration() / 12.0));
        float newIntrestRate = year * Float.parseFloat(get_increment);
        double interstRate = Double.parseDouble(get_loan_type)+newIntrestRate;
        double Round_interstRate = Math.round(interstRate * 100.00)/100.00;
        String SelectedInterestRate = String.valueOf(Round_interstRate);

        Loan loan = new Loan();
        loan.setLoanAmount(newLoanDTO.getLoanAmount());
        loan.setLoanType(newLoanDTO.getLoanType());
        loan.setDuration(newLoanDTO.getDuration());
        loan.setInterestRate(Float.parseFloat(SelectedInterestRate));
        if(newLoanDTO.getLoanAmount() > 1000000.00){
            loan.setApproved(false);
            loan.setStatus("Pending");
        }
        else {
            loan.setApproved(true);
            loan.setStartTime(new Date());
            loan.setStatus("Start");
        }
        loan.setAccount(accountDetails);
        var loanResponse = loanRepo.save(loan);

        LoanPaymentDTO loanPaymentDTO = new LoanPaymentDTO();
        loanPaymentDTO.setLoan(loanResponse.getLoanNumber());
        double oneMonthInterest = oneMonthRate(newLoanDTO.getLoanAmount(),Float.parseFloat(SelectedInterestRate));
        double emi  =calculateEmi(newLoanDTO.getLoanAmount(),Float.parseFloat(SelectedInterestRate),newLoanDTO.getDuration());
        loanPaymentDTO.setPrincipleAmount(Math.round((emi-oneMonthInterest)*100.00)/100.00);
        loanPaymentDTO.setInterestAmount(Math.round(oneMonthInterest * 100.00)/100.00);
        double amount_with_interest = Math.round(emi*newLoanDTO.getDuration() * 100.0) / 100.0;
        loanPaymentDTO.setPaymentAmount(amount_with_interest);
        double roundAmount = Math.round(emi * 100.0) / 100.0;
        loanPaymentDTO.setDueAmount(roundAmount);
        loanPaymentDTO.setEmiAmount(roundAmount);
        loanPaymentDTO.setScheduleDate(LocalDate.now().plusDays(30));
        loanPaymentDTO.setAccountNum(newLoanDTO.getAccountNum());
        loanPaymentDTO.setLoanAmount(newLoanDTO.getLoanAmount());
        loanPaymentDTO.setEmail(accountDetails.getAccountUser().getEmail());
        loanPaymentDTO.setLoan(loanResponse.getLoanNumber());
        if(newLoanDTO.getLoanAmount() < 1000000.00) {
            producer.sendLoanDetailsToTransaction(loanPaymentDTO);
        }

        return new ResponseDTO("200","Loan Request sent",null);


    }


    public static double calculateEmi(double principal, double annualInterestRate, int months) {
        double monthlyRate = annualInterestRate / 12 / 100;

        double emi = (principal * monthlyRate * Math.pow(1 + monthlyRate, months)) /
                (Math.pow(1 + monthlyRate, months) - 1);

        return emi;
    }

    public static double oneMonthRate(double principal, double annualInterestRate) {

        double r = annualInterestRate / 12.00 / 100;
//        double interest;
        return principal * r;

    }
}
