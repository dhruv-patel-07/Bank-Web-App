package com.bank.web.app.transaction.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DebitTransactionDTO {

    private Long reviverAccount;
    private Long senderAccount;
    private Double amount;
    @Pattern(regexp = "upi|neft|cash|rtgs", message = "Payment method must be either 'upi','neft','cash','rtgs'")
    private String method;

}
