package com.bank.web.app.notification.Kafka;

import com.bank.web.app.notification.email.EmailService;
import com.bank.web.app.notification.email.EmailServiceWithAttachment;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.internals.Acknowledgements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.kafka.support.Acknowledgment;

@Service
@Slf4j
public class NotificationConsumer {

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailServiceWithAttachment emailAttachmentService;



    @KafkaListener(topics = "${kafkaTopic.topic}")
    public void consumeAuthEmailTopic(AuthDto authDto) throws MessagingException {
        log.info("Consuming the message from Auth-Topic:: {} ",authDto);

        try {
            emailService.sendEmailVerification(
                    authDto.email(),
                    authDto.url(),
                    authDto.fname()
            );
        }catch (MessagingException exception){
            log.error("Email sending failed...{}",exception.getMessage());
        }
    }

    @KafkaListener(topics = "${kafkaTopic.accountTopic}")
//    if use with manual check
//    public void consumeNewAccountTopic(AccountKafkaDTO accountKafkaDTO, Acknowledgment ack) throws MessagingException {
        public void consumeNewAccountTopic(AccountKafkaDTO accountKafkaDTO) throws MessagingException {

        log.info("Consuming the message from Account-Topic:: {} ",accountKafkaDTO);
        try{
            emailService.sendEmailAccountActive(accountKafkaDTO.getEmail(),accountKafkaDTO);
//            ack.acknowledge();
        }catch (MessagingException exception){
            log.error("Email sending failed...{}",exception.getMessage());
        }


    }

//Edited
    @KafkaListener(topics = "${kafkaTopic.TransactionEmail}",groupId = "transactionGroup")
    public void consumeTransactionEmil(TransactionEmail transactionEmail) throws MessagingException {
        log.info("Consuming the message from Transaction-Topic:: {} ",transactionEmail);

        try {
            emailService.sendTransactionEmail(transactionEmail.getEmail(),transactionEmail);
        }catch (MessagingException exception){
            log.error("Email sending failed transaction-topic...{}",exception.getMessage());
        }
    }

    @KafkaListener(topics = "${kafkaTopic.loan}", groupId = "loanApprovedGroup")
    public void consumeLoanApprovalEmail(LoanPaymentDTO loanPaymentDTO) throws MessagingException {
        log.info("Consuming the message from Loan-Topic:: {} ",loanPaymentDTO);

        try {
            emailService.sendLoanApprovalEmail(loanPaymentDTO.getEmail(),loanPaymentDTO);
        }catch (MessagingException exception){
            log.error("Email sending failed loan-topic...{}",exception.getMessage());
        }
    }

    @KafkaListener(topics = "${kafkaTopic.loanFailed}", groupId = "loanFailedGroup")
    public void consumeLoanPaymentEmail(LoanPaymentFailedDTO loanPaymentFailedDTO) throws MessagingException {
        log.info("Consuming the message from failedLoan-Topic:: {} ",loanPaymentFailedDTO);

        try {
            emailService.sendFailedLoanPaymentEmail(loanPaymentFailedDTO.getEmail(),loanPaymentFailedDTO);
        }catch (MessagingException exception){
            log.error("Email sending failed FailedLoan-topic...{}",exception.getMessage());
        }
    }

    @KafkaListener(topics = "${kafkaTopic.loanReminder}")
    public void consumeReminderPayment(LoanPaymentFailedDTO loanPaymentFailedDTO) throws MessagingException {
        log.info("Consuming the message from ReminderEmail-Topic:: {} ",loanPaymentFailedDTO);

        try {
          emailService.sendEmailReminder(loanPaymentFailedDTO.getEmail(),loanPaymentFailedDTO);
        }catch (MessagingException exception){
            log.error("Email sending failed Reminder-topic...{}",exception.getMessage());
        }
    }

    @KafkaListener(topics = "${kafkaTopic.statementEmail}")
    public void consumeStatementEmail(EmailWithAttachmentDTO emailWithAttachmentDTO) {
        log.info("Consuming the message from Statement-Email-Topic::{}",emailWithAttachmentDTO);
        try {
            emailAttachmentService.sendEmailWithTemplate(emailWithAttachmentDTO.getEmail(),emailWithAttachmentDTO.getFileData(),emailWithAttachmentDTO.getFileName(),emailWithAttachmentDTO);
        }catch (Exception exception){
            log.error("Email sending failed Statement-Email-topic...{}",exception.getMessage());
        }

    }

    @KafkaListener(topics = "${kafkaTopic.requestStatement}")
    public void consumeRequestEmail(EmailWithAttachmentDTO emailWithAttachmentDTO) {
        log.info("Consuming the message from Requested-statement-Email-Topic::{}",emailWithAttachmentDTO);
        try {
            emailAttachmentService.sendEmailWithTemplate(emailWithAttachmentDTO.getEmail(),emailWithAttachmentDTO.getFileData(),emailWithAttachmentDTO.getFileName(),emailWithAttachmentDTO);
        }catch (Exception exception){
            log.error("Email sending failed requested-statement-Email-topic...{}",exception.getMessage());
        }

    }

    @KafkaListener(topics = "${kafkaTopic.freezeAccount}")
    public void consumeFreezeAccount(FreezeAccount freezeAccount) {
        log.info("Consuming the message from freeze-account-topic::{}",freezeAccount);
        try {
            emailService.sendFreezeAccountEmail(freezeAccount.getEmail(),freezeAccount);
        }catch (Exception exception){
            log.error("Email sending failed freeze-account-topic...{}",exception.getMessage());
        }

    }

}
