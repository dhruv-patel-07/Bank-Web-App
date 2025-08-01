package com.bank.web.app.account.dto;

import lombok.Data;


@Data
public class UserInfoDTO {
    private String name;
    private String accountNum;
    private String branch;
    private String type;
    private String uid;
    private String email;
}
