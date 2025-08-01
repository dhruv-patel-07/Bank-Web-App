package com.bank.web.app.download.exception;

public class InvalidAccountRequest extends RuntimeException {
    public InvalidAccountRequest(String message) {
        super(message);
    }
}
