package com.example.examen.repo;

import com.example.examen.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInRepo extends JpaRepository<CheckIn, Long> {
}
