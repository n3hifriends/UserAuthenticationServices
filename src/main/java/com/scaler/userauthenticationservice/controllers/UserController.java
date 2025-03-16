package com.scaler.userauthenticationservice.controllers;

import com.scaler.userauthenticationservice.dtos.UserDto;
import com.scaler.userauthenticationservice.models.User;
import com.scaler.userauthenticationservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("{email}")
    public UserDto getUserByEmail(@PathVariable String email) {
        User user = userService.findUserByEmail(email);
        if(user == null) {
            return null;
        }
        return fromUser(user);
    }

    @GetMapping("/userId/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        User user = userService.getUserDetails(id);
        if(user == null) return null;
        return fromUser(user);
    }

    public UserDto fromUser(User user){
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
//        userDto.setRoles(user.getRoles());
        return userDto;
    }
}
