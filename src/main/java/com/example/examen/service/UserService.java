package com.example.examen.service;

import com.example.examen.model.User;
import com.example.examen.repo.UserRepo;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepo userRepo;
    public User getCurrentUser() { //Hämtar inloggad användare
        //skapar en variabel som hämtar inloggad användare
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //hämtar användaren från databasen
        Optional<User> userOptional = userRepo.findByUsername(username);
        //returnerar användaren
        return userOptional.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
    //Metod för att hämta användare från databasen
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //optional för att hämta användare från databasen med användarnamn och om användaren inte finns kastas ett fel
        Optional<User> optionalUser = userRepo.findByUsername(username);
        //returnerar användaren om den finns
        if(optionalUser.isPresent()){
            return optionalUser.get();
            //annars kastas ett fel
        }else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}
