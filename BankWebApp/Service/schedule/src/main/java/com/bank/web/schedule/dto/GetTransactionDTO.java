package com.bank.web.schedule.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetTransactionDTO {
    private Long accountNum;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
