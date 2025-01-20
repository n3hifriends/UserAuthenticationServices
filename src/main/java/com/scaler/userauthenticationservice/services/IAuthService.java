package com.scaler.userauthenticationservice.services;

import com.scaler.userauthenticationservice.exceptions.PasswordMismatchException;
import com.scaler.userauthenticationservice.exceptions.UserAlreadyExistException;
import com.scaler.userauthenticationservice.exceptions.UserNotRegisteredException;
import com.scaler.userauthenticationservice.models.User;
import org.antlr.v4.runtime.misc.Pair;

public interface IAuthService {
    User signUp(String email, String password) throws UserAlreadyExistException;
    Pair<User, String> login(String email, String password) throws UserNotRegisteredException, PasswordMismatchException;

    Boolean validateToken(String token, Long userId);
}
