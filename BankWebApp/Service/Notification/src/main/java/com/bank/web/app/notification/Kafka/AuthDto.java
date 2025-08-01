package com.bank.web.app.notification.Kafka;

public record AuthDto(
        String email,
        String url,
        String fname
) {
}
