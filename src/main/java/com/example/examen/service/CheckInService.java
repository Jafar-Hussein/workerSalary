package com.example.examen.service;

import com.example.examen.adminDTO.CheckInInfoDTO;
import com.example.examen.dto.CheckInAdjustmentDTO;
import com.example.examen.dto.CheckInDTO;
import com.example.examen.dto.CheckOutAdjustmentDTO;
import com.example.examen.dto.CheckOutDTO;
import com.example.examen.model.CheckIn;
import com.example.examen.model.CheckOut;
import com.example.examen.model.Roles;
import com.example.examen.model.User;
import com.example.examen.repo.CheckInRepo;
import com.example.examen.repo.CheckOutRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckInService {
    private final CheckInRepo checkInRepo;
    private final UserService userService;
    private final EmployeeService employeeService;
    private final CheckOutRepo checkOutRepo;
    private final SalaryService salaryService;

    //check in
    public void checkIn() {
        User user = userService.getCurrentUser();
        if (user != null && user.getEmployee() != null) {
            CheckIn checkIn = new CheckIn();
            checkIn.setEmployee(user.getEmployee());
            checkIn.setCheckInDateTime(LocalDateTime.now()); // Set the check-in time to the current time
            checkInRepo.save(checkIn);
        } else {
            throw new IllegalArgumentException("User or associated employee not found");
        }
    }


    // Method to get all employee names and check-in comments
    // for the admin dashboard
    public List<CheckInInfoDTO> getAllEmployeeCheckIns() {
        List<CheckIn> checkIns = checkInRepo.findAll();

        return checkIns.stream().map(checkIn -> {
            CheckInInfoDTO dto = new CheckInInfoDTO();
            String fullName = checkIn.getEmployee().getFirstName() + " " + checkIn.getEmployee().getLastName();
            dto.setEmployeeName(fullName);
            dto.setCheckInTime(checkIn.getCheckInDateTime());
            return dto;
        }).collect(Collectors.toList());
    }

    //user
    public List<CheckInDTO> getCheckInsByEmployeeId() {
        User user = userService.getCurrentUser();
        if (user != null && user.getEmployee() != null) {
            return checkInRepo.findAllByEmployeeId(user.getEmployee().getId()).stream()
                    .map(checkIn -> new CheckInDTO(checkIn.getId(), checkIn.getCheckInDateTime()))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("User or associated employee not found");
        }

    }
    @Transactional
    public CheckIn adjustCheckInTime(Long checkInId, CheckInAdjustmentDTO adjustmentDTO) {
        User currentUser = userService.getCurrentUser();
        CheckIn checkIn = checkInRepo.findById(checkInId)
                .orElseThrow(() -> new IllegalArgumentException("CheckIn not found with id: " + checkInId));

        if (checkIn.getEmployee().getUser().equals(currentUser) || currentUser.getRole().equals(Roles.ADMIN)) {
            LocalDateTime newCheckInDateTime = adjustmentDTO.getNewCheckInDateTime();

            // Update check-in time
            checkIn.setCheckInDateTime(newCheckInDateTime);
            checkInRepo.save(checkIn);

            // Find the corresponding check-out after the adjusted check-in
            Optional<CheckOut> nextCheckOut = checkOutRepo.findFirstByEmployeeIdAndCheckOutDateTimeAfterOrderByCheckOutDateTimeAsc(
                    checkIn.getEmployee().getId(), newCheckInDateTime);

            if (nextCheckOut.isPresent()) {
                // A corresponding checkout is found, so proceed with salary calculation
                LocalDateTime nextCheckOutTime = nextCheckOut.get().getCheckOutDateTime();
                salaryService.updateWorkedHoursAndRecalculateSalary(checkIn.getEmployee().getId(), newCheckInDateTime, nextCheckOutTime);
            } else {
                // No corresponding checkout found. Handle accordingly.
                // For example, log this information or throw a custom exception if needed.
                System.out.println("No corresponding checkout found for the adjusted check-in for employeeId: " + checkIn.getEmployee().getId());
                // Depending on business logic, you might want to perform different actions here.
            }

            return checkIn;
        } else {
            throw new IllegalStateException("Unauthorized to adjust this check-in");
        }
    }



}