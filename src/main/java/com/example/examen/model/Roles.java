package com.example.examen.model;

import org.springframework.security.core.GrantedAuthority;

public enum Roles implements GrantedAuthority {
    // enum för att definiera roller
    ADMIN, USER;
    @Override
    public String getAuthority() {
        return name();
    }
}
