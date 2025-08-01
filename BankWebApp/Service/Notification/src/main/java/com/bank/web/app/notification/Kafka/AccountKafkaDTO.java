package com.bank.web.app.notification.Kafka;

import lombok.Data;

@Data
public class AccountKafkaDTO {

    private String uid;
    private Long accountNum;
    private String accountType;
    private double balance;
    private String email;
    private String branchName;
    private String branchId;
    private String name;


}
