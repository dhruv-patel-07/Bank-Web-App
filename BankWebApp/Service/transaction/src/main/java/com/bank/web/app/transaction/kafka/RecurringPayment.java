package com.bank.web.app.transaction.kafka;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RecurringPayment {
    private LocalDate scheduleDate;
    private Double paymentAmount;//
    private Float interestRate;
    private Double paidAmount;//
    private Double sipAmount;
    private int month;
    private Double dueAmount;
    private LocalDateTime paymentDate;//
    private Double totalAmount;
    private Long racId;
    private Long accountNum;
    private String email;
}
