package com.bank.web.app.transaction.dto;

import lombok.Data;

@Data
public class UserInfoDTO {
    private String name;
    private String accountNum;
    private String branch;
    private String type;
}
