package com.bank.web.app.account.dto;

import lombok.Data;

@Data
public class RecurringAccountCalculator {
    private double monthlyAmount;
    private int months;
}
