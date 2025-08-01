package com.bank.web.app.download.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionDataDTO {
    private Long tId;
    private String transactionType;
    private LocalDateTime timeStamp;
    private double amount;
    private double affected_balance;
    private String transactionMethod;
    private String remark;
    private Double balance;

}
