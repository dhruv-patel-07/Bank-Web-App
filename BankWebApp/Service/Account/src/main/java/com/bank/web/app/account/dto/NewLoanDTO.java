package com.bank.web.app.account.dto;

import com.bank.web.app.account.validatoin.AccountNumberValidation;
import com.bank.web.app.account.validatoin.AccountTypeValidation;
import lombok.Data;

@Data
public class NewLoanDTO {
    @AccountNumberValidation
    private Long accountNum;
    private String loanType;
    private Double loanAmount;
    private int duration;
}
