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
@RequiredArgsConstructor // Skapar en konstruktor för att automatiskt injicera beroenden
public class AuthService {
    private final UserRepo userRepo; // Används för att interagera med databasen för användare
    private Roles role; // Rollobjekt för att definiera användarens roll

    private final PasswordEncoder passwordEncoder; // Används för att kryptera lösenord

    private final AuthenticationManager authenticationManager; // Hanterar autentisering av användare
    private final TokenService tokenService; // Tjänst för att generera JWT-token

    // Metod för att registrera en ny användare
    public ResponseEntity<?> register(AuthRequest authRequest){
        try{
            // Kontrollera om användarnamnet redan finns i databasen
            Optional<User> existingUser = userRepo.findByUsername(authRequest.getUsername());
            if(existingUser.isPresent()){
                // Returnera ett felmeddelande om användarnamnet redan är upptaget
                return ResponseEntity.badRequest().body("Error: Username is already taken!");
            }
            // Kryptera användarens lösenord
            String encryptedPassword = passwordEncoder.encode(authRequest.getPassword());

            // Skapa en ny användare och ställ in användarnamn, lösenord och roll
            User user = new User();
            user.setUsername(authRequest.getUsername());
            user.setPassword(encryptedPassword);
            user.setRole(role.USER); // Sätt användarens roll som vanlig användare
            userRepo.save(user); // Spara användaren i databasen
            return ResponseEntity.ok("User registered successfully!"); // Returnera ett framgångsmeddelande
        }catch (Exception e){
            // Returnera ett felmeddelande om något går fel
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Metod för att logga in en användare
    public ResponseEntity<ResponseMessage> login(AuthRequest authRequest){
        try {
            // Autentisera användaren med användarnamn och lösenord
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );
            // Generera en JWT-token för den autentiserade användaren
            String jwt = tokenService.generateJwt(auth);
            // Returnera JWT-tokenen som svar
            return ResponseEntity.ok(new ResponseMessage(jwt));
        }catch (Exception e){
            // Returnera ett felmeddelande om autentiseringen misslyckas
            return ResponseEntity.badRequest().body(new ResponseMessage("Error: " + e.getMessage()));
        }
    }
}
