package com.bank.web.app.transaction.dto;

import com.bank.web.app.transaction.validatoin.AccountNumValidation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FreezeAccount {
    private Long account;
    private String desc;
    private boolean freeze;
    private String email;
}
