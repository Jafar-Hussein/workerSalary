package com.example.examen.config;

import com.example.examen.util.KeyProperties;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity // Aktiverar Spring Security för webbapplikationen
@EnableMethodSecurity // Möjliggör säkerhet på metodnivå
public class SecurityConfig {

    private final KeyProperties keys;

    // Konstruktor för att injicera nycklar för JWT
    public SecurityConfig(KeyProperties keys) {
        this.keys = keys;
    }

    // Konfigurera lösenordskryptering med BCryptPasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Konfigurera autentisering med användardetaljer och lösenordskryptering
    @Bean
    public AuthenticationManager authManager(UserDetailsService detailsService) {
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(detailsService); // Sätt UserDetailsService för att ladda användardetaljer
        daoProvider.setPasswordEncoder(passwordEncoder()); // Sätt PasswordEncoder till BCrypt
        return new ProviderManager(daoProvider); // Skapa en autentiseringshanterare
    }

    // Konfigurera säkerhetsfilterkedjan för HTTP-säkerhet och resursåtkomst
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Inaktiverar CSRF-skydd för enkel API-hantering
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/auth/**").permitAll(); // Tillåt åtkomst till autentiseringsrelaterade endpoints
                    auth.requestMatchers("/admin/**").hasRole("ADMIN"); // Endast administratörer kan komma åt admin endpoints
                    auth.requestMatchers("/user/**").hasAnyRole("ADMIN", "USER"); // Tillåt både användare och admin
                    auth.requestMatchers("/check-in/**").hasAnyRole("ADMIN", "USER"); // Check-in tillgänglig för användare och admin
                    auth.requestMatchers("/check-out/**").hasAnyRole("ADMIN", "USER"); // Check-out för användare och admin
                    auth.requestMatchers("/employee/**").hasAnyRole("ADMIN", "USER"); // Endpoints för anställda
                    auth.requestMatchers("/salary/**").hasAnyRole("ADMIN", "USER"); // Löneendpoints tillgängliga för båda
                    auth.requestMatchers("/leave-request/**").hasAnyRole("ADMIN", "USER"); // Ledighetsansökningar för båda roller
                    auth.anyRequest().authenticated(); // Alla andra requests kräver autentisering
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults())) // Konfigurera JWT som autentiseringsmetod
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Ingen session hanteras, stateless JWT
                .httpBasic(withDefaults()) // Aktivera grundläggande HTTP-autentisering
                .build();
    }

    // Konfigurera JWT-dekodning för att validera JWT-tokens
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(keys.getPublicKey()).build(); // Använd den offentliga nyckeln för att dekoda JWT
    }

    // Konfigurera JWT-kodare för att skapa JWT-tokens med privat nyckel
    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(keys.getPublicKey()).privateKey(keys.getPrivateKey()).build(); // Bygg en RSA-nyckel för JWT
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk)); // Skapa en immutabel JWKSet med nyckeln
        return new NimbusJwtEncoder(jwks); // Använd NimbusJwtEncoder för att koda JWT
    }

    // Konfigurera JWT-autentiseringsomvandlare för att hämta roller från JWT
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles"); // Definiera claim där roller finns i JWT
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_"); // Prefix för roller, t.ex. "ROLE_USER"
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter); // Sätt konverteraren för att hämta roller
        return jwtConverter;
    }

}
