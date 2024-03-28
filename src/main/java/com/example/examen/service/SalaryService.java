package com.example.examen.service;

import com.example.examen.dto.SalaryDTO;
import com.example.examen.model.CheckIn;
import com.example.examen.model.CheckOut;
import com.example.examen.model.Employee;
import com.example.examen.model.Salary;
import com.example.examen.repo.CheckInRepo;
import com.example.examen.repo.CheckOutRepo;
import com.example.examen.repo.EmployeeRepo;
import com.example.examen.repo.SalaryRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.Comparator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SalaryService {
    private final SalaryRepo salaryRepo;
    private final EmployeeRepo employeeRepo;
    private final CheckInRepo checkInRepo;
    private final CheckOutRepo checkOutRepo;


    @Transactional
    public Salary setSalaryDetails(Long employeeId, SalaryDTO salaryDTO) {
        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Salary salary = salaryRepo.findByEmployeeAndMonth(employee, salaryDTO.getMonth())
                .orElseGet(() -> {
                    Salary newSalary = new Salary();
                    newSalary.setEmployee(employee);
                    newSalary.setMonth(salaryDTO.getMonth());
                    return newSalary;
                });

        salary.setHourlyRate(salaryDTO.getHourlyRate());
        // Save or update the salary information
        return salaryRepo.save(salary);
    }

    // Calculate and set the total salary based on worked hours and hourly rate
    @Transactional
    public Salary calculateTotalSalary(Long employeeId, YearMonth month) {
        Salary salary = salaryRepo.findByEmployeeIdAndMonth(employeeId, month)
                .orElseThrow(() -> new IllegalArgumentException("Salary not found for the given month"));

        BigDecimal totalSalary = calculateWorkedHours(employeeId, month)
                .multiply(salary.getHourlyRate());
        salary.setTotalSalary(totalSalary);

        return salaryRepo.save(salary);
    }

    private BigDecimal calculateWorkedHours(Long employeeId, YearMonth month) {
        LocalDate startOfMonth = month.atDay(1);
        LocalDate endOfMonth = month.atEndOfMonth();

        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(23, 59, 59);

        List<CheckIn> checkIns = checkInRepo.findByEmployeeIdAndCheckInDateTimeBetween(employeeId, startDateTime, endDateTime);
        List<CheckOut> checkOuts = checkOutRepo.findByEmployeeIdAndCheckOutDateBetween(employeeId, startDateTime, endDateTime);

        // Assuming there is exactly one CheckOut for each CheckIn
        BigDecimal totalHours = BigDecimal.ZERO;

        for (CheckIn checkIn : checkIns) {
            CheckOut correspondingCheckOut = checkOuts.stream()
                    .filter(co -> co.getEmployee().getId().equals(checkIn.getEmployee().getId()) && !co.getCheckOutDate().isBefore(checkIn.getCheckInDateTime()))
                    .findFirst()
                    .orElse(null);

            if (correspondingCheckOut != null) {
                long hours = Duration.between(checkIn.getCheckInDateTime(), correspondingCheckOut.getCheckOutDate()).toHours();
                totalHours = totalHours.add(BigDecimal.valueOf(hours));
            }
        }

        return totalHours;
    }

}
