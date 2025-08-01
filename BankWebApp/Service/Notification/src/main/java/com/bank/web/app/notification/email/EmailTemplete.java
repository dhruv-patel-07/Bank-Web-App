package com.bank.web.app.notification.email;

import lombok.Getter;

public enum EmailTemplete {

    AUTH_LINK("auth-template.html","Email Verification"),
    ACCOUNT_ACTIVATION("account-active.html","Bank account activated 🥳"),
    CREDIT_TRANSACTION("credit-transaction.html","Credited to Your Account"),
    DEBIT_TRANSACTION("debit-transaction.html","Debited from Your Account"),
    LOAN_DEBIT_TRANSACTION("debit-transaction.html","Automatically loan Emi Deducted "),
    LOAN_PAYMENT_FAILED("loan-payment-failed.html","Alert: EMI Auto-Debit Failed Due to Low Balance"),
    REMINDER_PAYMENT("payment-reminder.html","EMI Payment Reminder"),
    LOAN_APPROVE("loan-approve.html","Loan Approval Confirmation –"),
    STATEMENT_EMAIL("statement-email.html","Bank Statement "),
    RECURRING_PAYMENT("recurring-payment.html","SIP Payment Successful "),
    FD_BOOK("fd-book.html","FD Booked for "),
    FD_CREDIT("fd-credit.html","FD Closed Successful "),
    FREEZE_ACCOUNT("freeze-account.html","Your Account Has Been Temporarily Frozen"),
    UNFREEZE_ACCOUNT("unfreeze-account.html", "Your Account Has Been Successfully Unfrozen");


    @Getter
    private final String template;
    @Getter
    private final String subject;

    EmailTemplete(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }

}
