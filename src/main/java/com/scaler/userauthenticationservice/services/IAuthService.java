package com.scaler.userauthenticationservice.services;

import com.scaler.userauthenticationservice.exceptions.PasswordMismatchException;
import com.scaler.userauthenticationservice.exceptions.UserAlreadyExistException;
import com.scaler.userauthenticationservice.exceptions.UserNotRegisteredException;
import com.scaler.userauthenticationservice.models.User;

public interface IAuthService {
    User signUp(String email, String password) throws UserAlreadyExistException;
    User login(String email, String password) throws UserNotRegisteredException, PasswordMismatchException;
}
