package com.example.examen.repo;

import com.example.examen.model.CheckOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckOutRepo extends JpaRepository<CheckOut, Long> {
}
