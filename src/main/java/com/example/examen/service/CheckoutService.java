package com.example.examen.service;

import com.example.examen.adminDTO.CheckOutInfoDTO;
import com.example.examen.dto.CheckOutAdjustmentDTO;
import com.example.examen.dto.CheckOutDTO;
import com.example.examen.model.CheckIn;
import com.example.examen.model.CheckOut;
import com.example.examen.model.User;
import com.example.examen.repo.CheckInRepo;
import com.example.examen.repo.CheckOutRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private static final Logger logger = LoggerFactory.getLogger(CheckoutService.class);
    private final CheckOutRepo checkOutRepo;
    private final UserService userService;
    private final EmployeeService employeeService;
    private final CheckInRepo checkInRepo;
    @Autowired
    private final SalaryService salaryService;

    //check out
    // In CheckoutService class
    @Transactional
    public void checkOut() {
        try {
            User user = userService.getCurrentUser();
            if (user != null && user.getEmployee() != null) {
                LocalDateTime now = LocalDateTime.now();
                // Ensure a Salary entry for the new month exists before saving the check-out
                salaryService.findOrCreateSalaryByEmployeeIdAndMonth(user.getEmployee().getId(), YearMonth.from(now));

                CheckOut checkOut = new CheckOut();
                checkOut.setEmployee(user.getEmployee());
                checkOut.setCheckOutDateTime(now);
                checkOutRepo.save(checkOut);

                // Update worked hours and recalculate salary
                salaryService.updateWorkedHoursAndRecalculateSalary(user.getEmployee().getId());
            } else {
                throw new IllegalArgumentException("User or associated employee not found");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Validation error during checkout process: {}", e.getMessage());
            throw e;  // Re-throw to be handled or logged by the caller/controller
        } catch (Exception e) {
            logger.error("Error during checkout process", e);
            throw new RuntimeException("Internal server error during checkout", e);
        }
    }



    // Method to get all employee names and check-in
    // for the admin dashboard
    public ResponseEntity<List<CheckOutInfoDTO>> getAllEmployeeCheckOuts() {
        List<CheckOut> checkOuts = checkOutRepo.findAll();

        return ResponseEntity.ok(checkOuts.stream().map(checkOut -> {
            CheckOutInfoDTO dto = new CheckOutInfoDTO();
            String fullName = checkOut.getEmployee().getFirstName() + " " + checkOut.getEmployee().getLastName();
            dto.setEmployeeName(fullName);
            dto.setCheckOutTime(checkOut.getCheckOutDateTime());
            return dto;
        }).collect(Collectors.toList()));
    }

    //user
    public List<CheckOutDTO> getCheckOutsByEmployeeId() {
        User user = userService.getCurrentUser();
        if (user != null && user.getEmployee() != null) {
            return checkOutRepo.findAllByEmployeeId(user.getEmployee().getId()).stream()
                    .map(checkOut -> new CheckOutDTO(checkOut.getId(), checkOut.getCheckOutDateTime()))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("User or associated employee not found");
        }
    }


    @Transactional
    public CheckOut adjustCheckOutTime(Long checkOutId, CheckOutAdjustmentDTO adjustmentDTO) {
        User currentUser = userService.getCurrentUser();
        CheckOut checkOut = checkOutRepo.findById(checkOutId)
                .orElseThrow(() -> new IllegalArgumentException("Check-out not found with id: " + checkOutId));

        if (currentUser.isCheckOutAdjustmentAllowed(checkOut)) {
            checkOut.setCheckOutDateTime(adjustmentDTO.getNewCheckOutDateTime());
            checkOutRepo.save(checkOut);

            // Call the updated method without date-time parameters
            salaryService.updateWorkedHoursAndRecalculateSalary(checkOut.getEmployee().getId());
        } else {
            throw new IllegalStateException("Unauthorized to adjust this check-out");
        }
        return checkOut;
    }
}
