package com.scaler.userauthenticationservice.services;

import com.scaler.userauthenticationservice.exceptions.PasswordMismatchException;
import com.scaler.userauthenticationservice.exceptions.UserAlreadyExistException;
import com.scaler.userauthenticationservice.exceptions.UserNotRegisteredException;
import com.scaler.userauthenticationservice.models.Role;
import com.scaler.userauthenticationservice.models.Session;
import com.scaler.userauthenticationservice.models.Status;
import com.scaler.userauthenticationservice.models.User;
import com.scaler.userauthenticationservice.repos.SessionRepo;
import com.scaler.userauthenticationservice.repos.UserRepo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private SessionRepo sessionRepo;

    @Autowired
    SecretKey secretKey;


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
    public Pair<User, String> login(String email, String password) throws UserNotRegisteredException, PasswordMismatchException {
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UserNotRegisteredException("Please sign up first...");
        }

        User user = userOptional.get();
        if(!bCryptPasswordEncoder.matches(password, user.getPassword())){
//        if (!user.getPassword().equals(password)) {
            throw new PasswordMismatchException("Please add correct password...");
        }

        // Generating JWT
//        String message = "dfpoisdhfadsjf r9uqewrdjsfj das"; // tostring
//        byte[] content = message.getBytes(StandardCharsets.UTF_8);
//        String token = Jwts.builder().content(content).compact();

        Map<String, Object> payload = new HashMap<>();

        Long nowInMillis = System.currentTimeMillis();

        payload.put("iat", nowInMillis); // issued at
        payload.put("exp", nowInMillis+100000); //sec // expiry at + 24hrs
        payload.put("scope", user.getRoles());
        payload.put("userId", user.getId());
        payload.put("iss", "scaler"); // issuer

//        MacAlgorithm algorithm = Jwts.SIG.HS256;
//        SecretKey secretKey = algorithm.key().build();
        String token = Jwts.builder().claims(payload).signWith(secretKey).compact();

        Session session = new Session();
        session.setCreatedAt(new Date());
        session.setLastUpdatedAt(new Date());
        session.setToken(token);
        session.setUser(user);
        session.setStatus(Status.ACTIVE);
        sessionRepo.save(session);

        return new Pair<User, String>(user, token);
    }

    @Override
    public Boolean validateToken(String token, Long userId) {

        Optional<Session> sessionOptional = sessionRepo.findByTokenAndUserId(token, userId);
        if(sessionOptional.isEmpty()){
            return false;
//            throw new UserNotRegisteredException("Please sign up first...");
        }
        String persistedToken = sessionOptional.get().getToken();
        JwtParser jwtParser = (JwtParser) Jwts.parser().verifyWith(secretKey);
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();
//        Long tokenExp = claims.getExpiration().getTime();
        Long tokenExp = (Long) claims.get("exp");

        Long currentTime = System.currentTimeMillis();

        if(currentTime > tokenExp){
            Session session = sessionOptional.get();
            session.setStatus(Status.INACTIVE);
            sessionRepo.save(session);
//            throw new UserNotRegisteredException("Please login first...");
            return false;
        }
        return true;
    }
}
