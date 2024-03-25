package com.example.examen.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class KeyGenerator {
    // Generera ett RSA-nyckelpar
    public static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            // Skapa en instans av KeyPairGenerator för RSA
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

            // Initialisera generatorn med en nyckellängd på 2048 bitar
            keyPairGenerator.initialize(2048);

            // Generera ett RSA-nyckelpar
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            // Kasta ett IllegalStateException om något går fel
            throw new IllegalStateException();
        }
        return keyPair;
    }
}