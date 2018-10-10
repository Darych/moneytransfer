package com.github.darych.moneytransfer.services;

public class TransactionServiceException extends RuntimeException {
    public TransactionServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionServiceException(String message) {
        super(message, null);
    }
}