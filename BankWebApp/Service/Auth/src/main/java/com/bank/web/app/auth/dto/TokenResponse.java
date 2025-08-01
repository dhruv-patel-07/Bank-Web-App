package com.bank.web.app.auth.dto;

import lombok.Data;

@Data
public class TokenResponse {
    private String access_token;
    private String refresh_token;
    private String token_type;
    private long expires_in;
}
