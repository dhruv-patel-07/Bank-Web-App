package com.bank.web.app.download.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetTransactionDTO {
    private Long accountNum;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
