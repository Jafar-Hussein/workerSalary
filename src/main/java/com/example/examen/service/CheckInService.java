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
import java.time.YearMonth;
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
    // In CheckInService class
    public void checkIn() {
        User user = userService.getCurrentUser();
        if (user != null && user.getEmployee() != null) {
            LocalDateTime now = LocalDateTime.now();
            // Ensure a Salary entry for the new month exists before saving the check-in
            salaryService.findOrCreateSalaryByEmployeeIdAndMonth(user.getEmployee().getId(), YearMonth.from(now));

            CheckIn checkIn = new CheckIn();
            checkIn.setEmployee(user.getEmployee());
            checkIn.setCheckInDateTime(now); // Set the check-in time to the current time
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

        if (currentUser.isCheckInAdjustmentAllowed(checkIn)) {
            checkIn.setCheckInDateTime(adjustmentDTO.getNewCheckInDateTime());
            checkInRepo.save(checkIn);

            // Call the updated method without date-time parameters
            salaryService.updateWorkedHoursAndRecalculateSalary(checkIn.getEmployee().getId());
        } else {
            throw new IllegalStateException("Unauthorized to adjust this check-in");
        }
        return checkIn;
    }



}