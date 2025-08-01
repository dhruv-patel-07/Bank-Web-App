package com.bank.web.app.transaction.kafka.producer;

import lombok.Data;

@Data
public class TransactionEmail {
    private String amount;
    private String time;
    private String Account;
    private String Tid;
    private String email;
    private String type;
    private String method;
    private String acType;
    private String remark;
}
