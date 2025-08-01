package com.bank.web.app.account.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Interest {
    @Id
    @GeneratedValue
    private Integer ID;
    private float oneYearIncrement;
    private String loanType;
    private float fixedRate;

    @ManyToOne
    @JoinColumn(name = "branch_id") // foreign key column in InterestRate table
    private Branch branch;
}
