package com.bank.web.app.transaction.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringExclude;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long tId;
    private String transactionType;
    private LocalDateTime timeStamp;
    private double amount;
    private double affected_balance;
    private String transactionMethod;
    private String remark;
    @ManyToOne
    @JoinColumn(name = "account_id", referencedColumnName = "accountNum")
    private Account account;

    @OneToOne(mappedBy = "transaction")
    private LoanPayment loanPayment;

    @OneToOne(mappedBy = "transaction")
    private RecurringAccountPayment recurringAccountPayment;


}
