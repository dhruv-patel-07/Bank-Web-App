package com.bank.web.app.transaction.validatoin;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomAccountNumValidation.class)
public @interface AccountNumValidation {
    String message() default "The specified account was not found or is currently inactive. Please verify the account number.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
