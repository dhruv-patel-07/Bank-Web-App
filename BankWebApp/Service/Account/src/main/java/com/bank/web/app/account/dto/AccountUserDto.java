package com.bank.web.app.account.dto;

import com.bank.web.app.account.validatoin.AccountTypeValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountUserDto {

    @NotBlank(message = "Contact required!")
    private String contact;

    @NotBlank(message = "Aadhar number  required!")
    private String aadharNumber;

    @NotBlank(message = "Pan card required!")
    private String panCardNumber;

    @NotNull(message = "Branch Code Required")
    private String branch;

    @AccountTypeValidation
    private String accountType;

    private double monthlyAmount;

    private int months;

    private String sipDate;
}
