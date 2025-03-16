package com.scaler.userauthenticationservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.userauthenticationservice.clients.KafkaProducerClient;
import com.scaler.userauthenticationservice.dtos.EmailDto;
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

    @Autowired
    private KafkaProducerClient kafkaProducerClient;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

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
        role.setStatus(Status.ACTIVE);
        role.setCreatedAt(new Date());
        role.setLastUpdatedAt(new Date());
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        user.setRoles(roles);

        userRepo.save(user);

        //send message into Kafka
        try {
            EmailDto emailDto = new EmailDto();
            emailDto.setTo(email);
            emailDto.setFrom("n3.hifriends@gmail.com");
            emailDto.setSubject("User Registration");
            emailDto.setBody("Welcome to Scaler");

            kafkaProducerClient.sendMessage("signup", objectMapper.writeValueAsString(emailDto));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }

        return user;
    }

    @Override
    public Pair<User, String> login(String email, String password) throws UserNotRegisteredException, PasswordMismatchException {
        Optional<User> userOptional = userRepo.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UserNotRegisteredException("Please sign up first...");
        }

        User user = userOptional.get();
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
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
        payload.put("exp", nowInMillis + 100000); //sec // expiry at + 24hrs
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
        Optional<Session> optionalSession = sessionRepo.findByTokenAndUserId(token,userId);

        if(optionalSession.isEmpty()) {
            return false;
        }

        JwtParser jwtParser = Jwts.parser().verifyWith(secretKey).build();
        Claims claims = jwtParser.parseSignedClaims(token).getPayload();

        Long tokenExpiry = (Long) claims.get("exp");
        Long currentTime = System.currentTimeMillis();

        System.out.println(tokenExpiry);
        System.out.println(currentTime);

        if(currentTime > tokenExpiry) {
            Session session = optionalSession.get();
            session.setStatus(Status.INACTIVE);
            sessionRepo.save(session);
            return false;
        }

        return true;
    }
}
