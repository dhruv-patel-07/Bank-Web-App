package com.bank.web.schedule.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Document
public class LoanPayment {
    @Id
    private String id;
    private Long loanId;
    private Long paymentId;
    private String scheduleDate;
    private Double emi;
    private Double balance;
    private Boolean isReminderSend;
    private String email;

}
