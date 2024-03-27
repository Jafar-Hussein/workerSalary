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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
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
        // This logic assumes each check-in has a corresponding check-out; adjust as necessary for your logic
        List<CheckIn> checkIns = checkInRepo.findAllByEmployeeIdAndMonth(employeeId, month);
        List<CheckOut> checkOuts = checkOutRepo.findAllByEmployeeIdAndMonth(employeeId, month);

        BigDecimal totalHours = BigDecimal.ZERO;
        for (int i = 0; i < checkIns.size(); i++) {
            LocalDateTime checkInTime = checkIns.get(i).getCheckInDateTime();
            LocalDateTime checkOutTime = checkOuts.get(i).getCheckOutDate();
            totalHours = totalHours.add(BigDecimal.valueOf(Duration.between(checkInTime, checkOutTime).toHours()));
        }

        return totalHours;
    }

}
