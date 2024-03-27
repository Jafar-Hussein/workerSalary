package com.example.examen.repo;

import com.example.examen.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    // find by username and password
    Optional<User> findByUsernameAndPassword(String username, String password);
    // find by username
    Optional<User> findByUsername(String username);
}
