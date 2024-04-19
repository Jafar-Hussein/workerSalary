package com.example.examen.service;

import com.example.examen.model.AuthRequest;
import com.example.examen.model.ResponseMessage;
import com.example.examen.model.Roles;
import com.example.examen.model.User;
import com.example.examen.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepo userRepo;
    private Roles role;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public ResponseEntity<?> register(AuthRequest authRequest){
        try{
            Optional<User> existingUser = userRepo.findByUsername(authRequest.getUsername());
            if(existingUser.isPresent()){
                return ResponseEntity.badRequest().body("Error: Username is already taken!");
            }
            String encryptedPassword = passwordEncoder.encode(authRequest.getPassword());

            User user = new User();
            user.setUsername(authRequest.getUsername());
            user.setPassword(encryptedPassword);
            user.setRole(role.USER);
            userRepo.save(user);
            return ResponseEntity.ok("User registered successfully!");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseMessage> login(AuthRequest authRequest){
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            String jwt = tokenService.generateJwt(auth);
            return ResponseEntity.ok(new ResponseMessage(jwt));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(new ResponseMessage("Error: " + e.getMessage()));
        }
    }
}
