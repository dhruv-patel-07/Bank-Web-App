package com.bank.web.app.account.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
public class Branch {
    @Id
    @GeneratedValue
    private Long branchId;

    private String branchName;
    private String branchCode;
    private String address;
    private String phoneNumber;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<AccountDetails> accounts;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Interest> interestRates;
}
