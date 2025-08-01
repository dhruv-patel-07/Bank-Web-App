package com.bank.web.app.auth.dto;

public record ResponseDTO(
        String status,
        String message,
        Object object
) {
}
