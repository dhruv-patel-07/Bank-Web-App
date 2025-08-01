package com.bank.web.app.account.service;

import com.bank.web.app.account.dto.RecurringAccountCalculator;
import com.bank.web.app.account.dto.ResponseDTO;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecurringAccountService {
    public ResponseDTO calculateInterest(RecurringAccountCalculator data) {
            int months = data.getMonths();
            double amount = data.getMonthlyAmount();
            double interest = interestRate(months);
            double CompoundInterest = (amount)*((double) ((months) * (months + 1)) /2) * (interest/1200);
            double finalAmount = (amount*months) + CompoundInterest;
            Map<String,Object> map = new HashMap<>();
            map.put("month",months);
            map.put("sip",amount);
            map.put("interest",interest);
            map.put("TotalAmount",months*amount);
            map.put("CompoundAmount",finalAmount);
            return new ResponseDTO("200","Compound Interest Amount",map);
    }

    public double interestRate(int months){
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
}
