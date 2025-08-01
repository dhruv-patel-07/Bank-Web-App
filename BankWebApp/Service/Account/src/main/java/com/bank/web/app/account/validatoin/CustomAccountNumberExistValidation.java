package com.bank.web.app.account.validatoin;

import com.bank.web.app.account.model.AccountDetails;
import com.bank.web.app.account.repo.AccountDetailsRepo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomAccountNumberExistValidation implements ConstraintValidator<AccountNumberValidation,Long> {
    @Autowired
    private AccountDetailsRepo accountDetailsRepo;
    @Override
    public boolean isValid(Long acNo, ConstraintValidatorContext constraintValidatorContext) {
        if(accountDetailsRepo.existsByAccountNumber(acNo)){
            return true;
        }
        else {
            return false;
        }
    }
}
