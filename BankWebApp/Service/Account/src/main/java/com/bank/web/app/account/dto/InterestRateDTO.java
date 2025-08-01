package com.bank.web.app.account.dto;

import lombok.Data;

@Data
public class InterestRateDTO {


    private float oneYearIncrement;
    private String loanType;
    private float fixedRate;


}
