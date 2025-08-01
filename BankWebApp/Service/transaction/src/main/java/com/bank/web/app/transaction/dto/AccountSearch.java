package com.bank.web.app.transaction.dto;

import lombok.Data;

@Data
public class AccountSearch {

    private String name;
    private Long AccountNum;
    private String email;
    private boolean isActive;
    private boolean isFreeze;
    private Double balance;
}
