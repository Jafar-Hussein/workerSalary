package com.example.examen.service;

import com.example.examen.adminDTO.CheckInInfoDTO;
import com.example.examen.dto.CheckInAdjustmentDTO;
import com.example.examen.model.CheckIn;
import com.example.examen.model.User;
import com.example.examen.repo.CheckInRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CheckInService {
    private final CheckInRepo checkInRepo;
    private final UserService userService;
    private final EmployeeService employeeService;

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
        List<CheckIn> checkIns = checkInRepo.findAll(); // Assumes you want to get all check-ins

        List<CheckInInfoDTO> checkInInfoDTOS = checkIns.stream().map(checkIn -> {
            CheckInInfoDTO dto = new CheckInInfoDTO();
            String fullName = checkIn.getEmployee().getFirstName() + " " + checkIn.getEmployee().getLastName();
            dto.setEmployeeName(fullName);
            dto.setCheckInTime(checkIn.getCheckInDateTime());
            return dto;
        }).collect(Collectors.toList());

        return checkInInfoDTOS;
    }

    //user
    public List<CheckIn> getCheckInsByEmployeeId(Long employeeId) {
        return checkInRepo.findAllByEmployeeId(employeeId);
    }
    @Transactional
    public CheckIn adjustCheckInTime(Long checkInId, CheckInAdjustmentDTO adjustmentDTO) {
        User currentUser = userService.getCurrentUser();
        CheckIn checkIn = checkInRepo.findById(checkInId)
                .orElseThrow(() -> new IllegalArgumentException("CheckIn not found with id: " + checkInId));

        // Assuming the current user is allowed to adjust their check-in time
        // or you have additional checks for admin users
        if (checkIn.getEmployee().getUser().equals(currentUser)) {
            checkIn.setCheckInDateTime(adjustmentDTO.getNewCheckInDateTime());
            return checkInRepo.save(checkIn);
        } else {
            throw new IllegalStateException("Unauthorized to adjust this check-in");
        }
    }

}