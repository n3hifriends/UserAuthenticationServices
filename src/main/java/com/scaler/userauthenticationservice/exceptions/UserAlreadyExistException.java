package com.scaler.userauthenticationservice.exceptions;

public class UserAlreadyExistException extends Exception {
    public UserAlreadyExistException(String message) {
        super(message);
    }
}
