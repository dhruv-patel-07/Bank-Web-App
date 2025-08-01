package com.bank.web.app.auth.dto;

import com.bank.web.app.auth.Validation.UniqueEmailValidation;
import lombok.Data;

@Data
public class RegisterRequest {
    @UniqueEmailValidation
    String username;
//    @UniqueEmailValidation
    String email;
    String password;
    String firstName;
    String lastName;
}

