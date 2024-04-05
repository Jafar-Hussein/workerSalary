package com.example.examen.service;

import com.example.examen.dto.SalaryDTO;
import com.example.examen.model.*;
import com.example.examen.repo.CheckInRepo;
import com.example.examen.repo.CheckOutRepo;
import com.example.examen.repo.EmployeeRepo;
import com.example.examen.repo.SalaryRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalaryService {
    private final SalaryRepo salaryRepo;
    private final EmployeeRepo employeeRepo;
    private final CheckInRepo checkInRepo;
    private final CheckOutRepo checkOutRepo;
    private final UserService userService;


    public void setSalaryDetails(Long employeeId, SalaryDTO salaryDTO) {
        // Check if the month is provided in the DTO, if not set it to the current month and year
        if (salaryDTO.getMonth() == null) {
            salaryDTO.setMonth(YearMonth.now());
        }

        Salary salary = findOrCreateSalaryByEmployeeIdAndMonth(employeeId, salaryDTO.getMonth());
        salary.setHourlyRate(salaryDTO.getHourlyRate() != null ? salaryDTO.getHourlyRate() : BigDecimal.ZERO); // Fallback to 0 if null
        salary.setWorkedHours(salaryDTO.getWorkedHours() != null ? salaryDTO.getWorkedHours() : 0); // Fallback to 0 if null
        // For totalSalary, consider if you want to recalculate it here based on hourlyRate and workedHours,
        // or if you want to directly use the provided value (if any).
        salary.setTotalSalary(salaryDTO.getTotalSalary() != null ? salaryDTO.getTotalSalary() : BigDecimal.ZERO); // Fallback to 0 if null
        salaryRepo.save(salary);
    }


    public SalaryDTO getCurrentMonthSalary() {
        User user = userService.getCurrentUser();
        if (user == null || user.getEmployee() == null) {
            throw new IllegalArgumentException("Current user is not logged in or does not have an associated employee.");
        }
        Long employeeId = user.getEmployee().getId();

        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        YearMonth currentMonth = YearMonth.now();
        Salary salary = salaryRepo.findByEmployeeAndMonth(employee, currentMonth)
                .orElseThrow(() -> new IllegalArgumentException("Salary not found for employee ID: " + employeeId + " and current month"));

        return mapSalaryToDTO(salary);
    }


    public List<SalaryDTO> getPastSalaries(Long employeeId) {
        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        List<Salary> salaries = salaryRepo.findAllByEmployee(employee);
        return salaries.stream()
                .map(this::mapSalaryToDTO)
                .sorted(Comparator.comparing(SalaryDTO::getMonth).reversed())
                .collect(Collectors.toList());
    }

    private SalaryDTO mapSalaryToDTO(Salary salary) {
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setHourlyRate(salary.getHourlyRate());
        salaryDTO.setTotalSalary(salary.getTotalSalary());
        salaryDTO.setWorkedHours(salary.getWorkedHours());
        salaryDTO.setMonth(salary.getMonth());
        return salaryDTO;
    }
    //update hourly rate
    public ResponseEntity<?> updateHourlyRate(Long employeeId, BigDecimal hourlyRate) {
        Salary salary = salaryRepo.findByEmployeeIdAndMonth(employeeId, YearMonth.now())
                .orElseThrow(() -> new IllegalArgumentException("Salary not found for employee ID: " + employeeId + " and current month"));

        salary.setHourlyRate(hourlyRate);
        salaryRepo.save(salary);
        return ResponseEntity.ok("Hourly rate updated successfully");
    }

    @Transactional
    public void updateWorkedHoursAndRecalculateSalary(Long employeeId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Find or create the salary record for the employee for the given month.
        Salary salary = findOrCreateSalaryByEmployeeIdAndMonth(employeeId, YearMonth.from(startDateTime));

        // Fetch check-ins and check-outs for the employee within the specified datetime range.
        List<CheckIn> checkIns = checkInRepo.findByEmployeeIdAndCheckInDateTimeBetween(employeeId, startDateTime, endDateTime);
        List<CheckOut> checkOuts = checkOutRepo.findByEmployeeIdAndCheckOutDateTimeBetween(employeeId, startDateTime, endDateTime);

        // Sort both lists to ensure chronological order.
        checkIns.sort(Comparator.comparing(CheckIn::getCheckInDateTime));
        checkOuts.sort(Comparator.comparing(CheckOut::getCheckOutDateTime));

        BigDecimal totalWorkedHours = BigDecimal.ZERO;
        int checkOutIndex = 0; // Start with the first check-out.

        for (CheckIn checkIn : checkIns) {
            LocalDateTime checkInTime = checkIn.getCheckInDateTime();
            // Find the first check-out after this check-in.
            while (checkOutIndex < checkOuts.size()) {
                LocalDateTime checkOutTime = checkOuts.get(checkOutIndex).getCheckOutDateTime();
                if (!checkOutTime.isBefore(checkInTime)) {
                    BigDecimal hoursBetween = BigDecimal.valueOf(Duration.between(checkInTime, checkOutTime).toMinutes())
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                    totalWorkedHours = totalWorkedHours.add(hoursBetween);
                    checkOutIndex++; // Move to the next check-out for the next iteration.
                    break; // Exit the loop after finding the corresponding check-out.
                }
                checkOutIndex++; // This check-out was before the check-in, move to the next.
            }
        }
        System.out.println("Retrieved salary record for employeeId: " + employeeId + ", month: " + YearMonth.from(startDateTime) + ", with hourly rate: " + salary.getHourlyRate());


        // After calculating the total worked hours, check if the hourly rate is set.
        if (salary.getHourlyRate().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("Hourly rate not set for employeeId " + employeeId);
        }

        // Calculate and set the total salary.
        BigDecimal totalSalary = salary.getHourlyRate().multiply(totalWorkedHours);
        salary.setWorkedHours(totalWorkedHours.intValue()); // This loses the fractional part; consider storing as BigDecimal if needed.
        salary.setTotalSalary(totalSalary);

        // Save the updated salary record.
        salaryRepo.save(salary);
    }


    public Salary findOrCreateSalaryByEmployeeIdAndMonth(Long employeeId, YearMonth month) {
        return salaryRepo.findByEmployeeIdAndMonth(employeeId, month)
                .orElseGet(() -> {
                    Employee employee = employeeRepo.findById(employeeId)
                            .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));
                    // Fetch the most recent salary record for the employee to get the latest hourly rate
                    Salary mostRecentSalary = salaryRepo.findTopByEmployeeIdOrderByMonthDesc(employeeId).orElse(null);
                    BigDecimal lastHourlyRate = (mostRecentSalary != null) ? mostRecentSalary.getHourlyRate() : BigDecimal.ZERO; // Fallback to ZERO if no records are found

                    Salary newSalary = new Salary();
                    newSalary.setEmployee(employee);
                    newSalary.setMonth(month);
                    newSalary.setWorkedHours(0);
                    newSalary.setHourlyRate(lastHourlyRate); // Use the last known hourly rate
                    return salaryRepo.save(newSalary);
                });
    }





}
