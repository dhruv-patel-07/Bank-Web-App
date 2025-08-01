package com.bank.web.app.notification.email;

import com.bank.web.app.notification.Kafka.EmailWithAttachmentDTO;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

@Service
public class EmailServiceWithAttachment {
    @Autowired
    private MailProperties mailProperties;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendEmailWithTemplate(String to, byte[] arr, String attachmentFilename, EmailWithAttachmentDTO emailWithAttachmentDTO) throws IOException {
        // Mail properties
//        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
//        pdfStream.write(arr);
//        Files.write(Paths.get("debug.pdf"), arr);

        String username = "test";
        Properties props = new Properties();
//        props.put("mail.smtp.auth", mailProperties.getProperties().get("mail.smtp.auth"));
//        props.put("mail.smtp.starttls.enable", mailProperties.getProperties().get("mail.smtp.starttls.enable"));
//        props.put("mail.smtp.host", mailProperties.getHost());
//        props.put("mail.smtp.port", String.valueOf(mailProperties.getPort()));
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.host","localhost");
        props.put("mail.smtp.port", "1025");

        try {
            // Mail session
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("test", "test");
                }
            });
//            Session session = Session.getInstance(props);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("no-reply@JdbcBank.com")); // You can set a custom from
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(EmailTemplete.STATEMENT_EMAIL.getSubject() + emailWithAttachmentDTO.getTitle());

            // Prepare Thymeleaf HTML content
            Context context = new Context();
            context.setVariable("accountNum",emailWithAttachmentDTO.getAccountNum());
            context.setVariable("StatementPeriod",emailWithAttachmentDTO.getStartTime()+" to "+emailWithAttachmentDTO.getEndTime());
            String htmlContent = templateEngine.process(EmailTemplete.STATEMENT_EMAIL.getTemplate(), context);

            // HTML body part
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            // Attachment part
            MimeBodyPart attachmentPart = new MimeBodyPart();
//            byte[] pdfBytes = pdfStream.toByteArray();
            DataSource dataSource = new ByteArrayDataSource(arr, "application/pdf");
            attachmentPart.setDataHandler(new DataHandler(dataSource));
            attachmentPart.setFileName(attachmentFilename);

            // Combine parts
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);
            // Send
                Transport.send(message);
            System.out.println("Email sent successfully to " + to);

        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
