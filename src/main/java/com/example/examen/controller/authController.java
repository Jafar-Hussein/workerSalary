package com.example.examen.controller;

import com.example.examen.model.AuthRequest;
import com.example.examen.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/")
@RequiredArgsConstructor
public class authController {
    private final AuthService authService;

    @PostMapping("register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> register(@RequestBody AuthRequest authRequest){
        return authService.register(authRequest);
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest){
        return authService.login(authRequest);
    }
}
