package com.tasktracker.service;

import com.tasktracker.dto.LoginRequest;
import com.tasktracker.dto.RegisterRequest;
import com.tasktracker.dto.UserResponse;
import com.tasktracker.entity.User;
import com.tasktracker.exception.EmailAlreadyExistsException;
import com.tasktracker.exception.PasswordMismatchException;
import com.tasktracker.repository.UserRepository;
import com.tasktracker.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public String register(RegisterRequest req) {
        if (!req.password().equals(req.repeatPassword())) {
            throw new PasswordMismatchException();
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new EmailAlreadyExistsException(req.email());
        }
        User user = new User(req.email(), passwordEncoder.encode(req.password()));
        userRepository.save(user);
        emailService.sendWelcome(req.email());
        return jwtService.generate(req.email());
    }

    public String login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return jwtService.generate(req.email());
    }

    public UserResponse me(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }
}
