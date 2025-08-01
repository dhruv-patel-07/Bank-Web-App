package com.bank.web.app.account.service;

import com.bank.web.app.account.dto.AccountUserDto;
import com.bank.web.app.account.dto.EmployeeDTO;
import com.bank.web.app.account.dto.RecurringAccountCalculator;
import com.bank.web.app.account.dto.ResponseDTO;
import com.bank.web.app.account.model.*;
import com.bank.web.app.account.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

@Service
public class AccountService {

    @Autowired
    private AccountUserRepo accountUserRepo;
    @Autowired
    private AccountDetailsRepo accountDetailsRepo;
    @Autowired
    private ExtractTokenService extractTokenService;
    @Autowired
    private BranchRepo branchRepo;
    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private RecurringAccountService recurringAccountService;
    @Autowired
    private RecurringAccountRepo recurringAccountRepo;

    private final SecureRandom secureRandom = new SecureRandom();


    public ResponseDTO createAccount(String authHeader, AccountUserDto accountUserDto) throws ParseException {
        if(accountUserDto.getAccountType().equalsIgnoreCase("recurring")){
            if(accountUserDto.getMonthlyAmount() == 0 || accountUserDto.getSipDate().trim().isEmpty() || accountUserDto.getMonths() == 0){
                    return new ResponseDTO("200","sip amount,duration,date(14,30) not valid",null);
            }
        }

        Map<String, Object> userData = extractTokenService.extractValue(authHeader);
        boolean is_verify = (boolean) userData.get("verified");
        if (!is_verify) {
            return new ResponseDTO("401", "Email not verified", null);
        }

        if (!accountUserRepo.existsByUid(userData.get("uid").toString())) {
        AccountUser accountUser = new AccountUser();
        accountUser.setUid(userData.get("uid").toString());
        accountUser.setActive(true);
        accountUser.setAadharNumber(accountUserDto.getAadharNumber());
        accountUser.setPanCardNumber(accountUserDto.getPanCardNumber());
        accountUser.setContact(accountUserDto.getContact());
        accountUser.setCreatedOn(LocalDateTime.now());
        accountUser.setEmail(userData.get("email").toString());
        accountUser.setName(userData.get("name").toString());
        var ac = accountUserRepo.save(accountUser);
        }
        Branch branch = branchRepo.findByBranchCode(accountUserDto.getBranch());
        AccountDetails accountDetails = new AccountDetails();
        accountDetails.setAccountNumber(generateUniqueAccountNumber());
        accountDetails.setAccountType(accountUserDto.getAccountType().toUpperCase());
        accountDetails.setIsActive(false);
        accountDetails.setBalance(0d);
        accountDetails.setBranch(branch);
        accountDetails.setOpenDate(LocalDateTime.now());
        accountDetails.setAccountUser(accountUserRepo.findByUid(userData.get("uid").toString()));
        var acD = accountDetailsRepo.save(accountDetails);
//        accountUser.setAccountDetails(List.of(accountDetails));
//        accountUser.sa
        if(accountUserDto.getAccountType().equalsIgnoreCase("recurring")) {
            RecurringAccount recurringAccount = new RecurringAccount();
            recurringAccount.setAccount(acD);
            recurringAccount.setAccountNum(acD.getAccountNumber());
            recurringAccount.setIsApproved(true);
            recurringAccount.setMonth(accountUserDto.getMonths());
            LocalDate today = LocalDate.now();
            LocalDate firstDayOfNextNextMonth = today
                    .plusMonths(2)
                    .with(TemporalAdjusters.firstDayOfMonth());
            LocalDate startDate = firstDayOfNextNextMonth.plusMonths(Long.parseLong(accountUserDto.getSipDate()));
            LocalDate endDate = startDate.plusMonths(accountUserDto.getMonths());
            recurringAccount.setEndDate(endDate);
            recurringAccount.setStartDate(startDate);
            Map<String,Object> recAccount = calculateInterest(accountUserDto.getMonths(),accountUserDto.getMonthlyAmount());
            recurringAccount.setInterestRate((Float) recAccount.get("interest"));
            recurringAccount.setCompoundAmount((Double) recAccount.get("CompoundAmount"));
            recurringAccount.setSipAmount((Double) recAccount.get("sip"));
            recurringAccount.setTotalAmount((Double) recAccount.get("TotalAmount"));
            recurringAccountRepo.save(recurringAccount);
        }

        return new ResponseDTO("200","account opening process succeeded",null);


    }

    private Long generateRandom12DigitLong() {
        long min = 100_000_000_000L;
        long max = 999_999_999_999L;
        return min + (long)(secureRandom.nextDouble() * (max - min));
    }

    public Long generateUniqueAccountNumber() {
        Long number;
        do {
            number = generateRandom12DigitLong();
        } while (accountDetailsRepo.existsByAccountNumber(number));
        return number;
    }

    public ResponseDTO addEmployee(String authHeader,EmployeeDTO employeeDTO) throws ParseException {

        Map<String,Object> map = extractTokenService.extractValue(authHeader);
        if(employeeRepo.existsById(map.get("uid").toString())){
            return new ResponseDTO("200","Employee already exist",null);
        }
        Employee employee = new Employee();
//        var employeeDTO1 = employeeDTO;
        employee.setBranch(branchRepo.findByBranchCode(employeeDTO.getBranchId()));
        employee.setEmployeeId(map.get("uid").toString());
        employee.setPosition(employeeDTO.getPosition());
        employeeRepo.save(employee);
        return new ResponseDTO("200","employee created",null);

    }


    public Map<String,Object> calculateInterest(int months,double amount) {
        double interest = recurringAccountService.interestRate(months);
        double CompoundInterest = (amount)*((double) ((months) * (months + 1)) /2) * (interest/1200);
        double finalAmount = (amount*months) + CompoundInterest;
        double totalAmt = months*amount;
        Map<String,Object> map = new HashMap<>();
        map.put("month",months);
        map.put("sip",amount);
        map.put("interest",Float.parseFloat(String.valueOf(interest)));
        map.put("TotalAmount",totalAmt);
        map.put("CompoundAmount",finalAmount);
        return map;
    }

}
