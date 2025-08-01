package com.bank.web.app.transaction.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
public class LoanPayment {
    @Id
    @GeneratedValue
    private Long lpId;
    private LocalDate scheduleDate;
    private Double paymentAmount;
    private Double principleAmount;
    private Double interestAmount;
    private Double paidAmount;
    private Double dueAmount;
    private Double emiAmount;
    private LocalDateTime paymentDate;
    private Long loanNum;
    private Long accountNum;
//    private Double loanAmount;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

}
