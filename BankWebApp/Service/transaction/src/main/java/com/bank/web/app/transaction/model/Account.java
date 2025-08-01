package com.bank.web.app.transaction.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "account")
public class Account {

    @Id
    private Long accountNum;
    private String uid;
    private String accountType;
    private double balance;
    private String email;
    private String branchName;
    private String branchId;
    private String name;
    @Column(nullable = true)
    private boolean isActive;
    @Column(nullable = true)
    private boolean isFreeze;
    private String description;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Transaction> transactions;


}
