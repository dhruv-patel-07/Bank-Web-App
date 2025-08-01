package com.bank.web.app.account.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FdResponse {
    private Long fdNum;
    private Double amount;
    private Double maturityAmount;
    private LocalDate maturityDate;
    private Float interest;
    private String status;
}
