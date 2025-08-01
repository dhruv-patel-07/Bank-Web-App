package com.bank.web.app.account.dto;

import lombok.Data;

@Data
public class FdBookTransaction {
    private Double amount;
    private Long account;
    private Long fdID;
    private String secret;
    //fdID
}
