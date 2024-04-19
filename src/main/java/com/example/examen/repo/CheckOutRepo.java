package com.example.examen.repo;

import com.example.examen.model.CheckOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckOutRepo extends JpaRepository<CheckOut, Long> {

    List<CheckOut> findByEmployeeIdAndCheckOutDateTimeBetween(Long employeeId, LocalDateTime startDateTime, LocalDateTime endDateTime);


    List<CheckOut> findAllByEmployeeId(Long id);
    Optional<CheckOut> findFirstByEmployeeIdAndCheckOutDateTimeAfterOrderByCheckOutDateTimeAsc(Long employeeId, LocalDateTime after);

}
