package com.bank.web.app.account.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AccountDetails {

    @Id
    private Long accountNumber;

    private String accountType;

    private Double balance;

    private Boolean isActive;

    private LocalDateTime openDate;

    private LocalDateTime closeDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_user_id")
    private AccountUser accountUser;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loans;

    @OneToMany(mappedBy = "account",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<RecurringAccount> recurringAccounts;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FD> fdList;
}
