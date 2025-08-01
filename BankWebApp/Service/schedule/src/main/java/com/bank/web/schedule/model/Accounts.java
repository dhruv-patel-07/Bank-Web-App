package com.bank.web.schedule.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class Accounts {
    @Id
    private String id;
    private Long accountNum;
    private String branchId;
    private String type;
}
