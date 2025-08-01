package com.bank.web.app.auth.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class UserVerification {
    @Id
    private String userId;
    private String email;
    private String token;
    private boolean isVerified;
    private LocalDateTime sendAt;
    private LocalDateTime expiryDate;
}
