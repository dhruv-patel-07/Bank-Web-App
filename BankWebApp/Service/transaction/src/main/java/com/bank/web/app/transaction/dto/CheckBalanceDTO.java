package com.bank.web.app.transaction.dto;

import lombok.Data;

@Data
public class CheckBalanceDTO {
    private Long accountNum;
    private String uid;

}
