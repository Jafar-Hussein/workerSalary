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
        // Adjust the startDateTime to include yesterday
        LocalDateTime startDateTimeWithYesterday = startDateTime.minusDays(1);

        // Find or create the salary record for the employee for the given month.
        Salary salary = findOrCreateSalaryByEmployeeIdAndMonth(employeeId, YearMonth.from(startDateTime));

        // Fetch check-ins and check-outs for the employee within the specified datetime range.
        List<CheckIn> checkIns = checkInRepo.findByEmployeeIdAndCheckInDateTimeBetween(employeeId, startDateTimeWithYesterday, endDateTime);
        List<CheckOut> checkOuts = checkOutRepo.findByEmployeeIdAndCheckOutDateTimeBetween(employeeId, startDateTimeWithYesterday, endDateTime);

        // Sort both lists to ensure chronological order.
        checkIns.sort(Comparator.comparing(CheckIn::getCheckInDateTime));
        checkOuts.sort(Comparator.comparing(CheckOut::getCheckOutDateTime));

        BigDecimal totalWorkedHours = BigDecimal.ZERO;

        // Iterate through check-ins and calculate worked hours
        for (int i = 0; i < checkIns.size(); i++) {
            LocalDateTime checkInTime = checkIns.get(i).getCheckInDateTime();
            LocalDateTime checkOutTime;

            // Find corresponding check-out time for the current check-in
            if (i < checkOuts.size() && checkOuts.get(i).getCheckOutDateTime().isAfter(checkInTime)) {
                checkOutTime = checkOuts.get(i).getCheckOutDateTime();
            } else {
                // If there's no corresponding check-out, set the check-out time to the endDateTime
                checkOutTime = endDateTime;
            }

            // Calculate worked hours for the current check-in and check-out pair
            BigDecimal hoursBetween = BigDecimal.valueOf(Duration.between(checkInTime, checkOutTime).toMinutes())
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
            totalWorkedHours = totalWorkedHours.add(hoursBetween);
        }

        // Calculate the total salary
        BigDecimal hourlyRate = salary.getHourlyRate();
        BigDecimal totalSalary = hourlyRate.multiply(totalWorkedHours).setScale(2, RoundingMode.HALF_UP);

        // Update the salary record with the calculated total salary
        salary.setTotalSalary(totalSalary);

        // Update the salary record with the calculated total worked hours
        salary.setWorkedHours(totalWorkedHours.intValue());

        salaryRepo.save(salary);
    }




    //    private BigDecimal calculateWorkedHours(Long employeeId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
//        List<CheckIn> checkIns = checkInRepo.findByEmployeeIdAndCheckInDateTimeBetween(employeeId, startDateTime, endDateTime);
//        List<CheckOut> checkOuts = checkOutRepo.findByEmployeeIdAndCheckOutDateTimeBetween(employeeId, startDateTime, endDateTime);
//
//        checkIns.sort(Comparator.comparing(CheckIn::getCheckInDateTime));
//        checkOuts.sort(Comparator.comparing(CheckOut::getCheckOutDateTime));
//
//        BigDecimal totalWorkedHours = BigDecimal.ZERO;
//        int checkOutIndex = 0;
//
//        for (CheckIn checkIn : checkIns) {
//            LocalDateTime checkInTime = checkIn.getCheckInDateTime();
//            while (checkOutIndex < checkOuts.size()) {
//                LocalDateTime checkOutTime = checkOuts.get(checkOutIndex).getCheckOutDateTime();
//                if (!checkOutTime.isBefore(checkInTime)) {
//                    BigDecimal hoursBetween = BigDecimal.valueOf(Duration.between(checkInTime, checkOutTime).toMinutes())
//                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
//                    totalWorkedHours = totalWorkedHours.add(hoursBetween);
//                    checkOutIndex++;
//                    break;
//                }
//                checkOutIndex++;
//            }
//        }
//        return totalWorkedHours;
//    }
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

//
//    @Transactional
//    public void recalculateWorkedHoursAndSalary(Long employeeId, LocalDateTime oldDateTime, LocalDateTime newDateTime, boolean isCheckIn) {
//        // Fetch the latest salary record or create a new one if it doesn't exist
//        Salary salary = findOrCreateSalaryByEmployeeIdAndMonth(employeeId, YearMonth.now());
//
//        // Calculate the difference in worked hours caused by the adjustment
//        BigDecimal workedHoursDifference = calculateWorkedHoursDifference(employeeId, oldDateTime, newDateTime, isCheckIn);
//
//        // Adjust the worked hours. We need to ensure that we never set a negative number of worked hours.
//        BigDecimal currentWorkedHours = BigDecimal.valueOf(salary.getWorkedHours() != null ? salary.getWorkedHours() : 0);
//        BigDecimal newWorkedHours = currentWorkedHours.add(workedHoursDifference).max(BigDecimal.ZERO);
//
//        salary.setWorkedHours(newWorkedHours.intValue());
//
//        // Recalculate salary based on new worked hours. Ensure hourlyRate is not null.
//        BigDecimal hourlyRate = salary.getHourlyRate() != null ? salary.getHourlyRate() : BigDecimal.ZERO;
//        BigDecimal newTotalSalary = newWorkedHours.multiply(hourlyRate).setScale(2, RoundingMode.HALF_UP);
//
//        salary.setTotalSalary(newTotalSalary);
//
//        // Persist the updated salary
//        salaryRepo.save(salary);
//    }

    @Transactional
    public BigDecimal calculateWorkedHoursDifference(Long checkInId, LocalDateTime oldDateTime, LocalDateTime newDateTime, boolean isCheckIn) {
        BigDecimal hoursDifference;

        if (isCheckIn) {
            CheckIn checkIn = checkInRepo.findById(checkInId)
                    .orElseThrow(() -> new IllegalStateException("Check-in record not found"));
            // Perform necessary calculations using checkIn and possibly checkOutRepo
            // Example placeholder logic for hours calculation:
            long durationInMinutes = ChronoUnit.MINUTES.between(oldDateTime, newDateTime);
            hoursDifference = BigDecimal.valueOf(durationInMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        } else {
            CheckOut checkOut = checkOutRepo.findById(checkInId)
                    .orElseThrow(() -> new IllegalStateException("Check-out record not found"));
            // Perform necessary calculations using checkOut and possibly checkInRepo
            // Example placeholder logic for hours calculation:
            long durationInMinutes = ChronoUnit.MINUTES.between(oldDateTime, newDateTime);
            hoursDifference = BigDecimal.valueOf(durationInMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        }

        // Depending on whether newDateTime is after or before oldDateTime, you may need to negate this value
        if (newDateTime.isBefore(oldDateTime)) {
            hoursDifference = hoursDifference.negate();
        }

        return hoursDifference;
    }




}
