package com.bank.web.app.account.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "account_user")
public class AccountUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String uid;

    private String name;

    private String email;

    private String contact;


    private LocalDateTime createdOn;

    private Date updatedOn;

    private String aadharNumber;

    private String panCardNumber;

    private boolean isActive;

    @OneToMany(mappedBy = "accountUser",cascade = CascadeType.ALL)
    private List<AccountDetails> accountDetails;
}
