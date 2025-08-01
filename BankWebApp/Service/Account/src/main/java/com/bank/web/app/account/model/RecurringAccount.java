package com.bank.web.app.account.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class RecurringAccount {

    @Id
    @GeneratedValue
    private Long id;
    private Long accountNum;
    private Float interestRate;
    private Double sipAmount;
    private int month;
    private Double totalAmount;
    private Double compoundAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean status;
    private Boolean isApproved;
    @ManyToOne
    @JoinColumn(name = "account_id")
    private AccountDetails account;
}
