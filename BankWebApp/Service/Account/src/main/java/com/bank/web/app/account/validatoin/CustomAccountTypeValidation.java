package com.bank.web.app.account.validatoin;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CustomAccountTypeValidation implements ConstraintValidator<AccountTypeValidation,String> {

    @Override
    public boolean isValid(String accountType, ConstraintValidatorContext constraintValidatorContext) {
        if(accountType.equalsIgnoreCase("saving") || accountType.equalsIgnoreCase("current") || accountType.equalsIgnoreCase("recurring")){
            return true;
        }
        else {
            return false;
        }
    }
}
