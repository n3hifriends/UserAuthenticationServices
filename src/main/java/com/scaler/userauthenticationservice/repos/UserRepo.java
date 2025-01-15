package com.scaler.userauthenticationservice.repos;

import com.scaler.userauthenticationservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    User save(User user);
    Optional<User> findByEmail(String email);
}
