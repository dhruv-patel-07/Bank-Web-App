package com.bank.web.app.download.dto;

public record ResponseDTO(
        String status,
        String message,
        Object object
) {
}
