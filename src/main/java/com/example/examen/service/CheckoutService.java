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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final CheckOutRepo checkOutRepo;
    private final UserService userService;
    private final EmployeeService employeeService;
    private final CheckInRepo checkInRepo;
    private final SalaryService salaryService;

    //check out
    @Transactional
    public void checkOut() {
        User user = userService.getCurrentUser();
        if (user != null && user.getEmployee() != null) {
            LocalDateTime now = LocalDateTime.now();
            CheckOut checkOut = new CheckOut();
            checkOut.setEmployee(user.getEmployee());
            checkOut.setCheckOutDateTime(now);
            checkOutRepo.save(checkOut);

            Optional<CheckIn> lastCheckIn = checkInRepo.findFirstByEmployeeIdAndCheckInDateTimeBeforeOrderByCheckInDateTimeDesc(
                    user.getEmployee().getId(), now);

            if (lastCheckIn.isPresent()) {
                LocalDateTime lastCheckInTime = lastCheckIn.get().getCheckInDateTime();
                salaryService.updateWorkedHoursAndRecalculateSalary(user.getEmployee().getId(), lastCheckInTime, now);
            } else {
                // Handle cases where no corresponding check-in is found
                // Maybe throw an exception or return a default value
            }
        } else {
            throw new IllegalArgumentException("User or associated employee not found");
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

        if (checkOut.getEmployee().getUser().equals(currentUser) || currentUser.getRole().equals("ADMIN")) {
            checkOut.setCheckOutDateTime(adjustmentDTO.getNewCheckOutDateTime());
            CheckOut updatedCheckOut = checkOutRepo.save(checkOut);

            // Find the last check-in before the adjusted check-out
            Optional<CheckIn> lastCheckIn = checkInRepo.findFirstByEmployeeIdAndCheckInDateTimeBeforeOrderByCheckInDateTimeDesc(
                    checkOut.getEmployee().getId(), adjustmentDTO.getNewCheckOutDateTime());

            if (lastCheckIn.isPresent()) {
                LocalDateTime lastCheckInTime = lastCheckIn.get().getCheckInDateTime();
                // Update worked hours and recalculate salary after adjusting check-out time
                salaryService.updateWorkedHoursAndRecalculateSalary(checkOut.getEmployee().getId(), lastCheckInTime, adjustmentDTO.getNewCheckOutDateTime());
            } else {
                // Handle cases where no corresponding check-in is found
                // This might involve logging a warning or throwing an exception, depending on your application's requirements
                System.out.println("No corresponding check-in found for adjusted check-out");

            }

            return updatedCheckOut;
        } else {
            throw new IllegalStateException("Unauthorized to adjust this check-out");
        }
    }

}
