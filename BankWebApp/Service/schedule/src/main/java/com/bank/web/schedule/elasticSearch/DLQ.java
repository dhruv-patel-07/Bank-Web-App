package com.bank.web.schedule.elasticSearch;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Data
@Document(indexName = "dlq")
public class DLQ {
    @Id
    private String uid;

    private String tittle;

    private String exception;

    private Object object;

    private LocalDateTime timestamp;
}
