package com.bank.web.app.transaction.validatoin;

import com.bank.web.app.transaction.Repo.AccountRepo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class CustomAccountNumValidation implements ConstraintValidator<AccountNumValidation,Long> {

    @Autowired
    private AccountRepo accountRepo;
    @Override
    public boolean isValid(Long accountNum, ConstraintValidatorContext constraintValidatorContext) {
        if(accountRepo.existsByAccountNum(accountNum)){
            return true;
        }
        else {
            return false;
        }
    }
}
