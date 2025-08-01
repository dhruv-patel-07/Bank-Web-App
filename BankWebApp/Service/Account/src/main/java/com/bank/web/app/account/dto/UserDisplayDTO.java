package com.bank.web.app.account.dto;

import lombok.Data;

@Data
public class UserDisplayDTO {

    private String account;
    private String UID;
    private String aadharCard;
    private String panCard;
    private String email;
    private String type;
}
