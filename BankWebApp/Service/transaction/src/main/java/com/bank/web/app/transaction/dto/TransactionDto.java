package com.bank.web.app.transaction.dto;

import com.bank.web.app.transaction.validatoin.AccountNumValidation;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class TransactionDto {

    @AccountNumValidation
    private Long accountNumber;

    private double amount;

    @Pattern(regexp = "upi|neft|cash|rtgs", message = "Payment method must be either 'upi','neft','cash','rtgs'")
    private String method;

    private String remark;
}
