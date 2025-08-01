package com.bank.web.app.account.kafka;

import com.bank.web.app.account.model.Loan;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LoanPaymentDTO {
    private LocalDate scheduleDate;
    private Double paymentAmount;
    private Double principleAmount;
    private Double interestAmount;
    private Double paidAmount;
    private Double dueAmount;
    private Double emiAmount;
    private LocalDateTime paymentDate;
    private Long loan;
    private Long accountNum;
    private String email;
    private Double loanAmount;


    //    Mapped with loan


}
