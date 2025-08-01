package com.bank.web.app.notification.Kafka;

import lombok.Data;

@Data
public class FreezeAccount {
    private Long account;
    private String desc;
    private boolean freeze;
    private String email;
}
