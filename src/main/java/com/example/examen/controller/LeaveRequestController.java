package com.example.examen.controller;

import com.example.examen.dto.LeaveRequestDTO;
import com.example.examen.model.LeaveRequest;
import com.example.examen.model.User;
import com.example.examen.service.LeaveRequestService;
import com.example.examen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/leave-request/")
@RequiredArgsConstructor
@CrossOrigin(origins= "*")
public class LeaveRequestController {
    private final LeaveRequestService leaveRequestService;
    private final UserService userService;
    @PostMapping("create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createLeaveRequest(@RequestBody LeaveRequestDTO leaveRequestDTO) {
        LeaveRequest leaveRequest = leaveRequestService.createLeaveRequest(leaveRequestDTO);
        return ResponseEntity.ok("Leave request created successfully");
    }

    @PutMapping("/{leaveRequestId}/dates")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateLeaveRequestDates(@PathVariable Long leaveRequestId, @RequestBody LeaveRequestDTO leaveRequestDTO) {
        LeaveRequest updatedLeaveRequest = leaveRequestService.updateLeaveRequestDates(leaveRequestId, leaveRequestDTO);
        return ResponseEntity.ok("Leave request dates updated successfully");
    }

    @PutMapping("{leaveRequestId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateLeaveRequestStatus(@PathVariable Long leaveRequestId, @RequestParam String status) {
        LeaveRequest updatedLeaveRequest = leaveRequestService.updateLeaveRequestStatus(leaveRequestId, status);
        return ResponseEntity.ok("Leave request status updated successfully");
    }

    @GetMapping("all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllLeaveRequests() {
        Iterable<LeaveRequestDTO> leaveRequests = leaveRequestService.getAllLeaveRequests();
        return ResponseEntity.ok(leaveRequests);
    }

    @GetMapping("employee")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getLeaveRequestsByEmployeeId(){
        User currentUser = userService.getCurrentUser();
        if (currentUser == null || currentUser.getEmployee() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found or not logged in.");
        }

        Long id = currentUser.getEmployee().getId();
        Iterable<LeaveRequestDTO> leaveRequestDTOs = leaveRequestService.getLeaveRequestsByEmployeeId(id);
        return ResponseEntity.ok(leaveRequestDTOs);
    }

}

