package com.scaler.userauthenticationservice.controllers;

import com.scaler.userauthenticationservice.dtos.LoginRequest;
import com.scaler.userauthenticationservice.dtos.SignupRequest;
import com.scaler.userauthenticationservice.dtos.UserDto;
import com.scaler.userauthenticationservice.exceptions.PasswordMismatchException;
import com.scaler.userauthenticationservice.exceptions.UserAlreadyExistException;
import com.scaler.userauthenticationservice.exceptions.UserNotRegisteredException;
import com.scaler.userauthenticationservice.models.User;
import com.scaler.userauthenticationservice.services.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@RequestBody SignupRequest signupRequest){
        try {
            User user = authService.signUp(signupRequest.getEmail(), signupRequest.getPassword());
            return new ResponseEntity<>(fromUser(user), HttpStatus.CREATED);
        } catch (UserAlreadyExistException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> loging(@RequestBody LoginRequest loginRequest){
        try {
            User user = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
            return new ResponseEntity<>(fromUser(user), HttpStatus.OK);
        } catch (UserNotRegisteredException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (PasswordMismatchException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    public UserDto fromUser(User user){
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());
        return userDto;
    }
}
