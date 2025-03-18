package com.scaler.userauthenticationservice.security;

import com.scaler.userauthenticationservice.models.User;
import com.scaler.userauthenticationservice.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public abstract class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;


    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByEmail(username);
        if(user.isEmpty()){
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomUserDetails(user.get());
    }

}
