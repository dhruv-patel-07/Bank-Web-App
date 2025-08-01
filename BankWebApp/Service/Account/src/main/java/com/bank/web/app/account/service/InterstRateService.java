package com.bank.web.app.account.service;

import com.bank.web.app.account.dto.InterestRateDTO;
import com.bank.web.app.account.dto.ResponseDTO;
import com.bank.web.app.account.model.AccountDetails;
import com.bank.web.app.account.model.AccountUser;
import com.bank.web.app.account.model.Employee;
import com.bank.web.app.account.model.Interest;
import com.bank.web.app.account.repo.EmployeeRepo;
import com.bank.web.app.account.repo.InterstRateRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Map;

@Service
public class InterstRateService {

    @Autowired
    private InterstRateRepo interstRateRepo;

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private ExtractTokenService extractTokenService;

    public ResponseDTO addNew(String authHeader, InterestRateDTO interestRateDTO) throws ParseException {

        Map<String,Object> map = extractTokenService.extractValue(authHeader);
        String id = map.get("uid").toString();
        Employee e = employeeRepo.findByEmployeeId(id);
        Interest interest = new Interest();
        interest.setBranch(e.getBranch());
        interest.setFixedRate(interestRateDTO.getFixedRate());
        interest.setLoanType(interestRateDTO.getLoanType());
        interest.setOneYearIncrement(interestRateDTO.getOneYearIncrement());
        interstRateRepo.save(interest);
        return new ResponseDTO("200","Loan type insertion success",null);
    }


}
