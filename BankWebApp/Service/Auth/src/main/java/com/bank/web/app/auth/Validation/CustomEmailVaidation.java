package com.bank.web.app.auth.Validation;

import com.bank.web.app.auth.model.UserVerification;
import com.bank.web.app.auth.repo.VerificationRepo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomEmailVaidation implements ConstraintValidator<UniqueEmailValidation,String> {
    @Autowired
    VerificationRepo userVerification;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
            if(!userVerification.existsByEmail(email)){
            return true;
        }
        else {
            return false;
        }
    }
}
