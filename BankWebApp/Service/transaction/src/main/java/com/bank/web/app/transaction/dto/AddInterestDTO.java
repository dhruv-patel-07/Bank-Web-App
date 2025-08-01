package com.bank.web.app.transaction.dto;


import lombok.Data;

@Data
public class AddInterestDTO {
    private Long accountNumber;
    private double amount;
    private String method;
    private String remark;
}
