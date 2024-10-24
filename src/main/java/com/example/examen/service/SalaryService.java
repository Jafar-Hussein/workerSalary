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
import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Skapar en konstruktor för att automatiskt injicera beroenden
public class SalaryService {
    private final SalaryRepo salaryRepo; // Repository för att hantera Salary-databasoperationer
    private final EmployeeRepo employeeRepo; // Repository för att hantera Employee-databasoperationer
    private final CheckInRepo checkInRepo; // Repository för att hantera CheckIn-databasoperationer
    private final CheckOutRepo checkOutRepo; // Repository för att hantera CheckOut-databasoperationer
    private final UserService userService; // Tjänst för att hantera användarrelaterade operationer

    // Sätt löneinformation för en specifik anställd
    public void setSalaryDetails(Long employeeId, SalaryDTO salaryDTO) {
        // Om ingen månad anges, sätt den till nuvarande månad
        if (salaryDTO.getMonth() == null) {
            salaryDTO.setMonth(YearMonth.now());
        }

        // Hitta eller skapa en lönepost för den anställda och den angivna månaden
        Salary salary = findOrCreateSalaryByEmployeeIdAndMonth(employeeId, salaryDTO.getMonth());
        salary.setHourlyRate(salaryDTO.getHourlyRate() != null ? salaryDTO.getHourlyRate() : BigDecimal.ZERO); // Fallback till 0 om null
        salary.setWorkedHours(salaryDTO.getWorkedHours() != null ? salaryDTO.getWorkedHours() : 0); // Fallback till 0 om null
        salary.setTotalSalary(salaryDTO.getTotalSalary() != null ? salaryDTO.getTotalSalary() : BigDecimal.ZERO); // Fallback till 0 om null
        salaryRepo.save(salary); // Spara löneposten i databasen
    }

    // Hämta aktuell månadslön för nuvarande användare
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

        return mapSalaryToDTO(salary); // Konvertera Salary till SalaryDTO och returnera
    }

    // Hämta tidigare löner för en specifik anställd
    public List<SalaryDTO> getPastSalaries(Long employeeId) {
        Employee employee = employeeRepo.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        List<Salary> salaries = salaryRepo.findAllByEmployee(employee);
        return salaries.stream()
                .map(this::mapSalaryToDTO)
                .filter(dto -> dto.getMonth() != null)  // Filtrera bort DTOs med null-månad
                .sorted(Comparator.comparing(SalaryDTO::getMonth, Comparator.nullsLast(YearMonth::compareTo)).reversed()) // Sortera i omvänd ordning
                .collect(Collectors.toList());
    }

    // Mappa en Salary-entitet till en SalaryDTO
    private SalaryDTO mapSalaryToDTO(Salary salary) {
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setHourlyRate(salary.getHourlyRate());
        salaryDTO.setTotalSalary(salary.getTotalSalary());
        salaryDTO.setWorkedHours(salary.getWorkedHours());
        salaryDTO.setMonth(salary.getMonth());
        return salaryDTO;
    }

    // Uppdatera timlön för en anställd för den aktuella månaden
    public ResponseEntity<?> updateHourlyRate(Long employeeId, BigDecimal hourlyRate) {
        Salary salary = salaryRepo.findByEmployeeIdAndMonth(employeeId, YearMonth.now())
                .orElseThrow(() -> new IllegalArgumentException("Salary not found for employee ID: " + employeeId + " and current month"));

        salary.setHourlyRate(hourlyRate); // Uppdatera timlönen
        salaryRepo.save(salary); // Spara uppdateringen i databasen
        return ResponseEntity.ok("Hourly rate updated successfully");
    }

    // Uppdatera arbetade timmar och räkna om lön för en anställd
    @Transactional
    public void updateWorkedHoursAndRecalculateSalary(Long employeeId) {
        LocalDateTime startDateTime = YearMonth.now().atDay(1).atStartOfDay(); // Början av månaden
        LocalDateTime endDateTime = LocalDateTime.now(); // Nuvarande tid
        Salary salary = findOrCreateSalaryByEmployeeIdAndMonth(employeeId, YearMonth.from(startDateTime));

        List<CheckIn> checkIns = checkInRepo.findByEmployeeIdAndCheckInDateTimeBetween(employeeId, startDateTime, endDateTime);
        List<CheckOut> checkOuts = checkOutRepo.findByEmployeeIdAndCheckOutDateTimeBetween(employeeId, startDateTime, endDateTime);

        // Sortera check-ins och check-outs efter tid
        checkIns.sort(Comparator.comparing(CheckIn::getCheckInDateTime));
        checkOuts.sort(Comparator.comparing(CheckOut::getCheckOutDateTime));

        BigDecimal totalWorkedHours = BigDecimal.ZERO;

        for (CheckIn checkIn : checkIns) {
            Optional<CheckOut> matchingCheckOut = checkOuts.stream()
                    .filter(co -> !co.getCheckOutDateTime().isBefore(checkIn.getCheckInDateTime()))
                    .findFirst();

            if (matchingCheckOut.isPresent()) {
                long minutesBetween = Duration.between(checkIn.getCheckInDateTime(), matchingCheckOut.get().getCheckOutDateTime()).toMinutes();
                BigDecimal hoursWorked = BigDecimal.valueOf(minutesBetween).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                totalWorkedHours = totalWorkedHours.add(hoursWorked);
                checkOuts.remove(matchingCheckOut.get()); // Ta bort matchad check-out
            }
        }

        BigDecimal hourlyRate = salary.getHourlyRate();
        BigDecimal totalSalary = hourlyRate.multiply(totalWorkedHours).setScale(2, RoundingMode.HALF_UP); // Beräkna total lön
        salary.setTotalSalary(totalSalary);
        salary.setWorkedHours(totalWorkedHours.intValue());

        salaryRepo.save(salary); // Spara den uppdaterade löneposten
    }

    // Hjälpmetod för att hitta eller skapa en Salary-post baserat på anställd ID och månad
    public Salary findOrCreateSalaryByEmployeeIdAndMonth(Long employeeId, YearMonth month) {
        return salaryRepo.findByEmployeeIdAndMonth(employeeId, month)
                .orElseGet(() -> {
                    Employee employee = employeeRepo.findById(employeeId)
                            .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employeeId));
                    Salary mostRecentSalary = salaryRepo.findTopByEmployeeIdOrderByMonthDesc(employeeId).orElse(null);
                    BigDecimal lastHourlyRate = (mostRecentSalary != null) ? mostRecentSalary.getHourlyRate() : BigDecimal.ZERO; // Använd senaste timlön eller 0

                    Salary newSalary = new Salary();
                    newSalary.setEmployee(employee);
                    newSalary.setMonth(month);
                    newSalary.setWorkedHours(0);
                    newSalary.setHourlyRate(lastHourlyRate); // Använd senaste timlön
                    return salaryRepo.save(newSalary); // Spara den nya löneposten
                });
    }

    // Beräkna skillnad i arbetade timmar om en in-/utcheckningstid ändras
    @Transactional
    public BigDecimal calculateWorkedHoursDifference(Long checkInId, LocalDateTime oldDateTime, LocalDateTime newDateTime, boolean isCheckIn) {
        BigDecimal hoursDifference;

        if (isCheckIn) {
            CheckIn checkIn = checkInRepo.findById(checkInId)
                    .orElseThrow(() -> new IllegalStateException("Check-in record not found"));
            long durationInMinutes = ChronoUnit.MINUTES.between(oldDateTime, newDateTime);
            hoursDifference = BigDecimal.valueOf(durationInMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        } else {
            CheckOut checkOut = checkOutRepo.findById(checkInId)
                    .orElseThrow(() -> new IllegalStateException("Check-out record not found"));
            long durationInMinutes = ChronoUnit.MINUTES.between(oldDateTime, newDateTime);
            hoursDifference = BigDecimal.valueOf(durationInMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        }

        // Negera värdet om den nya tiden är tidigare än den gamla
        if (newDateTime.isBefore(oldDateTime)) {
            hoursDifference = hoursDifference.negate();
        }

        return hoursDifference;
    }
}
