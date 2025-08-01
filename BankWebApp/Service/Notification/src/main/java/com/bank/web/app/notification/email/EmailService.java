package com.bank.web.app.notification.email;

import com.bank.web.app.notification.Kafka.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private SpringTemplateEngine templateEngine;


    @Async
    public void sendEmailVerification(String toEmail,String url,String fname) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        messageHelper.setFrom("bank.xyz@bank.com");
        final String templateName = EmailTemplete.AUTH_LINK.getTemplate();
        Map<String,Object> variables = new HashMap<>();
        variables.put("email",toEmail);
        variables.put("url",url);
        variables.put("fname",fname);
        Context context = new Context();
        context.setVariables(variables);
        messageHelper.setSubject(EmailTemplete.AUTH_LINK.getSubject());
        try{
            String htmlTemplate = templateEngine.process(templateName,context);
            messageHelper.setText(htmlTemplate,true);
            messageHelper.setTo(toEmail);
            mailSender.send(mimeMessage);
            log.info("EMAIL-AUTH-LINK - Email sent to {} with template {} ",toEmail,templateName);

        }
        catch (MessagingException e){
            log.warn("WARNING - Can't send email to {}",toEmail);
        }
    }


    @Async
    public void sendEmailAccountActive(String toEmail, AccountKafkaDTO accountKafkaDTO) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        messageHelper.setFrom("billkaro.pos@gmail.com");
        final String templateName = EmailTemplete.ACCOUNT_ACTIVATION.getTemplate();
        Map<String,Object> variables = new HashMap<>();
        variables.put("email",toEmail);
        variables.put("accountNum",accountKafkaDTO.getAccountNum());
        variables.put("accountType",accountKafkaDTO.getAccountType());
        variables.put("branchName",accountKafkaDTO.getBranchName());
        variables.put("name",accountKafkaDTO.getName());
        Context context = new Context();
        context.setVariables(variables);
        messageHelper.setSubject(EmailTemplete.ACCOUNT_ACTIVATION.getSubject());
        try{
            String htmlTemplate = templateEngine.process(templateName,context);
            messageHelper.setText(htmlTemplate,true);
            messageHelper.setTo(toEmail);
            mailSender.send(mimeMessage);
            log.info("EMAIL-ACCOUNT-ACTIVATE - Email sent to {} with template {} ",toEmail,templateName);

        }
        catch (MessagingException e){
            log.warn("WARNING - Can't send email to ACCOUNT-ACTIVE{}",toEmail);
        }
    }


    @Async
    public void sendTransactionEmail(String toEmail, TransactionEmail transactionEmail) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        messageHelper.setFrom("billkaro.pos@gmail.com");
        String templateName = "";
        if(transactionEmail.getType().equalsIgnoreCase("credit")) {
            if(transactionEmail.getAcType().equalsIgnoreCase("recurring")) {
                templateName = EmailTemplete.RECURRING_PAYMENT.getTemplate();
            }
            else if (transactionEmail.getRemark().equalsIgnoreCase("FD")) {
                templateName = EmailTemplete.FD_CREDIT.getTemplate();
            } else if (transactionEmail.getType().equalsIgnoreCase("credit") && transactionEmail.getAcType().equalsIgnoreCase("saving") )
            {
                templateName = EmailTemplete.CREDIT_TRANSACTION.getTemplate();
            }
            else if (transactionEmail.getType().equalsIgnoreCase("credit") && transactionEmail.getAcType().equalsIgnoreCase("current") )
            {
                templateName = EmailTemplete.CREDIT_TRANSACTION.getTemplate();
            }
        }
        else if (transactionEmail.getType().equalsIgnoreCase("debit")){
            String[] parts = transactionEmail.getMethod().split(",");
            if(parts[0].equalsIgnoreCase("loan-payment")){
                templateName = EmailTemplete.LOAN_DEBIT_TRANSACTION.getTemplate();
            }
            else if (transactionEmail.getMethod().equalsIgnoreCase("FD")) {
                templateName = EmailTemplete.FD_BOOK.getTemplate();

            }
            else {
                templateName = EmailTemplete.DEBIT_TRANSACTION.getTemplate();
            }
        }
        Map<String,Object> variables = new HashMap<>();
        variables.put("email",toEmail);
        variables.put("accountNum",transactionEmail.getAccount());
        variables.put("amount",transactionEmail.getAmount());
        variables.put("date",transactionEmail.getTime());
        variables.put("referenceID",transactionEmail.getTid());
        variables.put("method",transactionEmail.getMethod());
        Context context = new Context();
        context.setVariables(variables);
        if(transactionEmail.getType().equalsIgnoreCase("credit")) {
            if(transactionEmail.getAcType().equalsIgnoreCase("recurring")){
                messageHelper.setSubject(EmailTemplete.RECURRING_PAYMENT.getSubject());
            }
                else if (transactionEmail.getRemark().equalsIgnoreCase("FD")) {
                    messageHelper.setSubject(EmailTemplete.FD_CREDIT.getSubject());
                }
            else if (transactionEmail.getType().equalsIgnoreCase("credit") && transactionEmail.getAcType().equalsIgnoreCase("saving") )
            {
                messageHelper.setSubject(EmailTemplete.CREDIT_TRANSACTION.getSubject());
            }
            else if (transactionEmail.getType().equalsIgnoreCase("credit") && transactionEmail.getAcType().equalsIgnoreCase("current") )
            {
                messageHelper.setSubject(EmailTemplete.CREDIT_TRANSACTION.getSubject());
            }
//
//            else {
//                messageHelper.setSubject(EmailTemplete.CREDIT_TRANSACTION.getSubject());
//            }
        }
        else if (transactionEmail.getType().equalsIgnoreCase("debit")){
            String[] parts = transactionEmail.getMethod().split(",");
            if(parts[0].equalsIgnoreCase("loan-payment")) {
                messageHelper.setSubject(EmailTemplete.LOAN_DEBIT_TRANSACTION.getSubject() + "Loan : "+parts[1]);
            } else if (transactionEmail.getMethod().equalsIgnoreCase("FD")) {
                messageHelper.setSubject(EmailTemplete.FD_BOOK.getSubject() + transactionEmail.getAmount());

            } else {
                messageHelper.setSubject(EmailTemplete.DEBIT_TRANSACTION.getSubject());
            }
        }
        try{
            String htmlTemplate = templateEngine.process(templateName,context);
            messageHelper.setText(htmlTemplate,true);
            messageHelper.setTo(toEmail);
            mailSender.send(mimeMessage);
            log.info("EMAIL-TRANSACTION - Email sent to {} with template {} ",toEmail,templateName);

        }
        catch (MessagingException e){
            log.warn("WARNING - Can't send email to EMAIL-TRANSACTION {}",toEmail);
        }
    }

    @Async
    public void sendLoanApprovalEmail(String toEmail, LoanPaymentDTO loanPaymentDTO) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        messageHelper.setFrom("billkaro.pos@gmail.com");
        final String templateName = EmailTemplete.LOAN_APPROVE.getTemplate();

        Map<String,Object> variables = new HashMap<>();

        variables.put("emiPrincipal",loanPaymentDTO.getPrincipleAmount());
        variables.put("emiInterest",loanPaymentDTO.getInterestAmount());
        variables.put("emi",loanPaymentDTO.getEmiAmount());
        variables.put("accountNumber",loanPaymentDTO.getAccountNum());
        variables.put("paymentDate",loanPaymentDTO.getScheduleDate());
        Context context = new Context();
        context.setVariables(variables);
            messageHelper.setSubject(EmailTemplete.LOAN_APPROVE.getSubject()+" "+loanPaymentDTO.getLoanAmount());
        try{
            String htmlTemplate = templateEngine.process(templateName,context);
            messageHelper.setText(htmlTemplate,true);
            messageHelper.setTo(toEmail);
            mailSender.send(mimeMessage);
            log.info("EMAIL-LOAN - Email sent to {} with template {} ",toEmail,templateName);

        }
        catch (MessagingException e){
            log.warn("WARNING -- Can't send email to EMAIL-LOAN {}",toEmail);
        }
    }

    @Async
    public void sendFailedLoanPaymentEmail(String toEmail, LoanPaymentFailedDTO loanPaymentFailedDTO) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        messageHelper.setFrom("billkaro.pos@gmail.com");
        final String templateName = EmailTemplete.LOAN_PAYMENT_FAILED.getTemplate();

        Map<String,Object> variables = new HashMap<>();

        variables.put("loanNumber",loanPaymentFailedDTO.getLoanNum());
        variables.put("nextDate",loanPaymentFailedDTO.getNextDate());
        variables.put("OverDueAmount",loanPaymentFailedDTO.getAmount());
        variables.put("accountNum",loanPaymentFailedDTO.getAccountNum());

        Context context = new Context();
        context.setVariables(variables);
        messageHelper.setSubject(EmailTemplete.LOAN_PAYMENT_FAILED.getSubject()+" Loan : "+loanPaymentFailedDTO.getLoanNum());
        try{
            String htmlTemplate = templateEngine.process(templateName,context);
            messageHelper.setText(htmlTemplate,true);
            messageHelper.setTo(toEmail);
            mailSender.send(mimeMessage);
            log.info("EMAIL-LOAN-Payment-Failed= - Email sent to {} with template {} ",toEmail,templateName);

        }
        catch (MessagingException e){
            log.warn("WARNING -- Can't send email to LOAN-Payment-Failed {}",toEmail);
        }
    }

    @Async
    public void sendEmailReminder(String toEmail,LoanPaymentFailedDTO loanPaymentFailedDTO) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        messageHelper.setFrom("bank.xyz@bank.com");
        final String templateName = EmailTemplete.REMINDER_PAYMENT.getTemplate();
        Map<String,Object> variables = new HashMap<>();
        variables.put("loanNumber",toEmail);
        variables.put("Date",loanPaymentFailedDTO.getNextDate());
        variables.put("Amount",loanPaymentFailedDTO.getAmount());
        String last4Digits = loanPaymentFailedDTO.getAccountNum().toString().substring(loanPaymentFailedDTO.getAccountNum().toString().length() - 4);
        variables.put("accountNum","XXXXXXXXXX"+last4Digits);

        Context context = new Context();
        context.setVariables(variables);
        messageHelper.setSubject(EmailTemplete.REMINDER_PAYMENT.getSubject());
        try{
            String htmlTemplate = templateEngine.process(templateName,context);
            messageHelper.setText(htmlTemplate,true);
            messageHelper.setTo(toEmail);
            mailSender.send(mimeMessage);
            log.info("EMAIL-REMINDER-LINK - Email sent to {} with template {} ",toEmail,templateName);

        }
        catch (MessagingException e){
            log.warn("WARNING - Can't send email reminder to {}",toEmail);
        }
    }

    @Async
    public void sendFreezeAccountEmail(String toEmail, FreezeAccount freezeAccount) throws MessagingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        messageHelper.setFrom("bank.xyz@bank.com");
        String templateName = "";
        if(freezeAccount.isFreeze()){
            templateName = EmailTemplete.FREEZE_ACCOUNT.getTemplate();
        }
        else {
            templateName = EmailTemplete.UNFREEZE_ACCOUNT.getTemplate();

        }
        Map<String,Object> variables = new HashMap<>();
//        variables.put("account",freezeAccount.getAccount());
        String last4Digits = freezeAccount.getAccount().toString().substring(freezeAccount.getAccount().toString().length() - 4);
        variables.put("Date", LocalDateTime.now());
        variables.put("remark",freezeAccount.getDesc());
        variables.put("accountNum","XXXXXXXXXX"+last4Digits);
        variables.put("isFreeze",freezeAccount.isFreeze());

        Context context = new Context();
        context.setVariables(variables);
        if(freezeAccount.isFreeze()){
        messageHelper.setSubject(EmailTemplete.FREEZE_ACCOUNT.getSubject());
        }
        else {
            messageHelper.setSubject(EmailTemplete.UNFREEZE_ACCOUNT.getSubject());
        }
        try{
            String htmlTemplate = templateEngine.process(templateName,context);
            messageHelper.setText(htmlTemplate,true);
            messageHelper.setTo(toEmail);
            mailSender.send(mimeMessage);
            log.info("EMAIL-FROZEN-REQUEST - Email sent to {} with template {} ",toEmail,templateName);

        }
        catch (MessagingException e){
            log.warn("WARNING - Can't send email reminder to {}",toEmail);
        }
    }
}
