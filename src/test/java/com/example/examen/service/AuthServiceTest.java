package com.example.examen.service;

import com.example.examen.model.AuthRequest;
import com.example.examen.model.ResponseMessage;
import com.example.examen.model.User;
import com.example.examen.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_successfulRegistration() {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("newUser");
        authRequest.setPassword("password123");
        when(userRepo.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encryptedPassword");

        // When
        ResponseEntity<?> response = authService.register(authRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully!", response.getBody());
        verify(userRepo).save(any(User.class));
    }
    @Test
    void register_usernameAlreadyExists() {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("existingUser");
        authRequest.setPassword("password123");
        when(userRepo.findByUsername("existingUser")).thenReturn(Optional.of(new User()));

        // When
        ResponseEntity<?> response = authService.register(authRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error: Username is already taken!", response.getBody());
    }
    @Test
    void register_withException() {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("newUser");
        authRequest.setPassword("password123");
        when(userRepo.findByUsername("newUser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenThrow(new RuntimeException("Encryption failure"));

        // When
        ResponseEntity<?> response = authService.register(authRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Error: Encryption failure"));
    }
    @Test
    void login_successfulLogin() {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("user");
        authRequest.setPassword("password");
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenService.generateJwt(authentication)).thenReturn("token123");

        // When
        ResponseEntity<ResponseMessage> response = authService.login(authRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token123", response.getBody().message());
    }
    @Test
    void login_withAuthenticationFailure() {
        // Given
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("user");
        authRequest.setPassword("wrongPassword");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        // When
        ResponseEntity<ResponseMessage> response = authService.login(authRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().message().contains("Error: Invalid credentials"));
    }

}