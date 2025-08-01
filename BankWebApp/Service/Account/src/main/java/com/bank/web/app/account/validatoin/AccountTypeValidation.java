package com.bank.web.app.account.validatoin;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomAccountTypeValidation.class)
public @interface AccountTypeValidation {
    String message() default "Account Type must be SAVING or CURRENT or RECURRING";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
