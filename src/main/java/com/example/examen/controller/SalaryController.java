package com.example.examen.controller;

import com.example.examen.dto.SalaryDTO;
import com.example.examen.model.User;
import com.example.examen.service.SalaryService;
import com.example.examen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@RestController
@RequestMapping("/salary/")
@RequiredArgsConstructorg
@CrossOrigin(origins= "*")
public class SalaryController {
    private final SalaryService salaryService;
    private final UserService userService;

    @GetMapping("user-salary")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getSalaryDetails() {
        return ResponseEntity.ok(salaryService.getCurrentMonthSalary());
    }

    @GetMapping("user-Salaries")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getSalaries() {
        User user = userService.getCurrentUser();
        if (user == null || user.getEmployee() == null) {
            return ResponseEntity.badRequest().body("User or associated employee not found");
        }
        Long employeeId = user.getEmployee().getId();
        return ResponseEntity.ok(salaryService.getPastSalaries(employeeId));
    }


    @PostMapping("set-salary/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setSalaryDetails(@PathVariable Long employeeId, @RequestBody SalaryDTO salaryDTO) {
        if (employeeId == null || salaryDTO == null) {
            return ResponseEntity.badRequest().body("Employee ID and salary details are required");
        }
        salaryService.setSalaryDetails(employeeId, salaryDTO);
        return ResponseEntity.ok("Salary details set successfully");
    }

//    @GetMapping("generate-initial-salary-record/{employeeId}/{year}/{month}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> generateInitialSalaryRecord(@PathVariable Long employeeId, @PathVariable int year, @PathVariable int month) {
//        SalaryDTO salaryDTO = new SalaryDTO();
//        salaryDTO.setMonth(YearMonth.of(year, month));
//        salaryService.setSalaryDetails(employeeId, salaryDTO);
//        return ResponseEntity.ok("Initial salary record generated successfully");
//    }

    @PutMapping("{hourlyRate}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> recalculateSalary(@PathVariable Long employeeId, @PathVariable BigDecimal hourlyRate) {
        salaryService.updateHourlyRate(employeeId, hourlyRate);
        return ResponseEntity.ok("Salary recalculated successfully");
    }

}
