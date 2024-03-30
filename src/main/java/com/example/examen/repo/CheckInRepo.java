package com.example.examen.repo;

import com.example.examen.model.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepo extends JpaRepository<CheckIn, Long> {
    List<CheckIn> findByEmployeeIdAndCheckInDateTimeBetween(Long employeeId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<CheckIn> findAllByEmployeeId(Long id);

    Optional<CheckIn> findFirstByEmployeeIdAndCheckInDateTimeBeforeOrderByCheckInDateTimeDesc(Long employeeId, LocalDateTime before);


}
