package com.scaler.userauthenticationservice.services;

import com.scaler.userauthenticationservice.exceptions.PasswordMismatchException;
import com.scaler.userauthenticationservice.exceptions.UserAlreadyExistException;
import com.scaler.userauthenticationservice.exceptions.UserNotRegisteredException;
import com.scaler.userauthenticationservice.models.Role;
import com.scaler.userauthenticationservice.models.User;
import com.scaler.userauthenticationservice.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public User signUp(String email, String password) throws UserAlreadyExistException {
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isPresent()) {
            throw new UserAlreadyExistException("Please try logging...");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setCreatedAt(new Date());
        user.setLastUpdatedAt(new Date());

        Role role = new Role();
        role.setValue("CUSTOMER");
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        user.setRoles(roles);

        userRepo.save(user);
        return user;
    }

    @Override
    public User login(String email, String password) throws UserNotRegisteredException, PasswordMismatchException {
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UserNotRegisteredException("Please sign up first...");
        }

        User user = userOptional.get();
        if(!bCryptPasswordEncoder.matches(password, user.getPassword())){
//        if (!user.getPassword().equals(password)) {
            throw new PasswordMismatchException("Please add correct password...");
        }

        return user;
    }
}
