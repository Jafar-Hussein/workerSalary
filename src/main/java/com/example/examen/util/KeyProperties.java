package com.example.examen.util;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Data
@Component
public class KeyProperties {
    // RSAPrivateKey för privat nyckel och RSAPublicKey för offentlig nyckel
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    // Konstruktör som genererar ett RSA-nyckelpar vid skapandet av en instans av klassen
    public KeyProperties() {
        // Använd KeyGenerator för att generera RSA-nyckelpar
        KeyPair par = KeyGenerator.generateRsaKey();

        // Sätt den offentliga nyckeln
        this.publicKey = (RSAPublicKey) par.getPublic();

        // Sätt den privata nyckeln
        this.privateKey = (RSAPrivateKey) par.getPrivate();
    }
}