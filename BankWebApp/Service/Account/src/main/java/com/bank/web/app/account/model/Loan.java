package com.bank.web.app.account.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long loanNumber;
    private Double loanAmount;
    private float interestRate;
    private int duration;
    private Date startTime;
    private Date endTime;
    private String status;
    private boolean isApproved;
    @ManyToOne
    @JoinColumn(name = "account_id")
    private AccountDetails account;
    private String loanType;
}
