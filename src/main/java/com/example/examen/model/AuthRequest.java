package com.example.examen.model;

import lombok.Data;

@Data
public class AuthRequest {
    //klass för att bara hämta användarnamn och lösenord
    private String username;
    private String password;
}
