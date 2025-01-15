package com.scaler.userauthenticationservice.exceptions;

public class PasswordMismatchException extends Exception {
    public PasswordMismatchException(String message) {
        super(message);
    }
}
