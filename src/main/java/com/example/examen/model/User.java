package com.example.examen.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private Roles role;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Employee employee;
    @Override
    public String getUsername() {
        return username;
    }
    public User() {

    }

    public User(String username, String password, Roles role, Employee employee) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.employee = employee;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(role);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isCheckOutAdjustmentAllowed(CheckOut checkOut) {
        // Allow the check-out adjustment if the user is a regular user or if the check-out belongs to the user or admin
        return this.getRole().equals(Roles.USER) || checkOut.getEmployee().getUser().equals(this) || this.getRole().equals(Roles.ADMIN);
    }

    public boolean isCheckInAdjustmentAllowed(CheckIn checkIn) {
        // Allow the check-in adjustment if the user is a regular user or if the check-in belongs to the user or admin
        return this.getRole().equals(Roles.USER) || checkIn.getEmployee().getUser().equals(this) || this.getRole().equals(Roles.ADMIN);
    }
}
