package com.example.examen.repo;

import com.example.examen.model.CheckOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Repository
public interface CheckOutRepo extends JpaRepository<CheckOut, Long> {

    List<CheckOut> findByEmployeeIdAndCheckOutDateBetween(Long employeeId, LocalDateTime startDateTime, LocalDateTime endDateTime);


}
