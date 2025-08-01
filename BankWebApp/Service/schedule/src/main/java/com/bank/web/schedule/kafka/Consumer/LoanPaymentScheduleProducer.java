package com.bank.web.schedule.kafka.Consumer;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanPaymentScheduleProducer {
    private Long loanId;
    private Long paymentId;
    private LocalDate date;
    private Double emi;
    private Double balance;
    private String email;
}
