package com.bank.web.app.transaction.Elasticsearch;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.annotation.Id;

@Data
@Document(indexName = "user")
public class UserDetails {
    @Id
    private String uid;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private Long accountNumber;

    @Field(type = FieldType.Keyword)
    private String email;

}
