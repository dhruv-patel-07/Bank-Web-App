package com.bank.web.app.transaction.kafka.producer;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanPaymentFailedDTO {
    private Long loanNum;
    private LocalDate nextDate;
    private Double amount;
    private Long accountNum;
    private String email;

}
