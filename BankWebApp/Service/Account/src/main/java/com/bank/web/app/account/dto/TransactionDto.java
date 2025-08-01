package com.bank.web.app.account.dto;

import com.bank.web.app.account.validatoin.AccountNumberValidation;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class TransactionDto {

    @AccountNumberValidation
    private Long accountNumber;

    private double amount;

    @Pattern(regexp = "upi|neft|cash|rtgs", message = "Payment method must be either 'upi','neft','cash','rtgs'")
    private String method;

    private String remark;
}
