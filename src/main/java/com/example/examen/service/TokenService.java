package com.example.examen.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class TokenService {

    @Autowired
    private JwtEncoder jwtEncoder;
    @Autowired private JwtDecoder jwtDecoder;

    /**
     * Den här metoden används för att generera en jwt token för användaren och admin.
     * Token används för att autentisera användaren när den använder API:et.
     * Beroende på användarens roll har användaren tillgång till olika delar av API:et.
     * Metoden kontrollerar också om jwtEncoder och jwtDecoder är korrekt konfigurerade.
     * @param auth är användarens autentisering.
     * @return en jwt token för användaren.
     */
    public String generateJwt(Authentication auth) {
        try {
            if (jwtEncoder == null || jwtDecoder == null) {
                throw new IllegalStateException("JwtEncoder or JwtDecoder is not properly configured.");
            }

            Instant now = Instant.now();

            String scope = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));

            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("self")
                    .issuedAt(now)
                    .subject(auth.getName())
                    .claim("roles", scope)
                    .build();

            return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating JWT";
        }
    }

}