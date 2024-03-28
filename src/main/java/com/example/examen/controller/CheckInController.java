package com.example.examen.controller;

import com.example.examen.dto.CheckInAdjustmentDTO;
import com.example.examen.service.CheckInService;
import com.example.examen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/check-in")
@RequiredArgsConstructor
public class CheckInController {
    private final CheckInService checkInService;
    private final UserService userService;

    @PostMapping("/")
    public ResponseEntity<?> checkIn() {
         checkInService.checkIn();
            return ResponseEntity.ok("Checked in successfully");
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllEmployeeCheckIns() {
        return ResponseEntity.ok(checkInService.getAllEmployeeCheckIns());
    }

    @GetMapping("/emp-check-ins")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCheckInsByEmployeeId() {
        return ResponseEntity.ok(checkInService.getCheckInsByEmployeeId());
    }
    // adjust check-in time. can be done by admin or user
    @PutMapping("/{checkInId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> adjustCheckInTime(@PathVariable Long checkInId, @RequestBody CheckInAdjustmentDTO adjustmentDTO) {
        if (userService.getCurrentUser() == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        checkInService.adjustCheckInTime(checkInId, adjustmentDTO);
        return ResponseEntity.ok("Check-in time adjusted successfully");
    }

}
