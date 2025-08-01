package com.bank.web.app.transaction.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class RecurringAccountPayment {
    @Id
    @GeneratedValue
    private Long pId;
    private LocalDate scheduleDate;
    private Double paymentAmount;
    private Float interestRate;
    private Double paidAmount;//
    private Double sipAmount;
    private int month;
    private int monthsCompleted;
    private Double dueAmount;
    private LocalDateTime paymentDate;
    private Double totalAmount;
    private Long racId;
    private Long accountNum;
    private String email;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
}
