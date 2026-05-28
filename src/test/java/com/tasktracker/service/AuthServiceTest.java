package com.tasktracker.service;

import com.tasktracker.dto.LoginRequest;
import com.tasktracker.dto.RegisterRequest;
import com.tasktracker.entity.User;
import com.tasktracker.exception.EmailAlreadyExistsException;
import com.tasktracker.exception.PasswordMismatchException;
import com.tasktracker.repository.UserRepository;
import com.tasktracker.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock EmailService emailService;
    @InjectMocks AuthService authService;

    @Test
    void register_success_returnsToken() {
        var req = new RegisterRequest("user@example.com", "pass1", "pass1");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass1")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(jwtService.generate("user@example.com")).thenReturn("jwt-token");

        String token = authService.register(req);

        assertThat(token).isEqualTo("jwt-token");
        verify(emailService).sendWelcome("user@example.com");
    }

    @Test
    void register_passwordMismatch_throws() {
        var req = new RegisterRequest("user@example.com", "pass1", "pass2");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(PasswordMismatchException.class);
    }

    @Test
    void register_duplicateEmail_throws() {
        var req = new RegisterRequest("dup@example.com", "pass1", "pass1");
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void login_success_returnsToken() {
        var req = new LoginRequest("user@example.com", "pass1");
        User user = new User("user@example.com", "hashed");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass1", "hashed")).thenReturn(true);
        when(jwtService.generate("user@example.com")).thenReturn("jwt-token");

        String token = authService.login(req);

        assertThat(token).isEqualTo("jwt-token");
    }

    @Test
    void login_wrongPassword_throws() {
        var req = new LoginRequest("user@example.com", "wrong");
        User user = new User("user@example.com", "hashed");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }
}
