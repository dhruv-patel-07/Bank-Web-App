package com.bank.web.app.transaction.enums;

public enum StatusCodeEnum {
    OK("200"),
    CREATED("201"),
    NOT_FOUND("404"),

    BAD_REQUEST("400"),
    EXCEPTION("500"),
    ERROR("501");

    StatusCodeEnum(String statusCode) {
        this.statusCode = statusCode;
    }

    private String statusCode;

    public String getStatusCode() {
        return statusCode;
    }
}
