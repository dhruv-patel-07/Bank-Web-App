package com.bank.web.app.account.validatoin;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomAccountNumberExistValidation.class)
public @interface AccountNumberValidation {

    String message() default "Account number not valid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
