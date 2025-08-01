package com.bank.web.app.account.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class FD {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private AccountDetails account;

    private int duration;

    private double amount;

    private double maturityAmount;

    private LocalDate startDate;

    private LocalDate maturityDate;

    private LocalDate withdrawalDate;

    private float interestRate;

    private Double interestAmount;

    private Double withdrawalAmount;

    private Boolean isClosed;



}
