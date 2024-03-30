package com.example.examen.service;

import com.example.examen.dto.LeaveRequestDTO;
import com.example.examen.model.Employee;
import com.example.examen.model.LeaveRequest;
import com.example.examen.repo.LeaveRequestRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeaveRequestService {
    private final LeaveRequestRepo leaveRequestRepo;
    private final UserService userService;
    @Transactional
    public LeaveRequest createLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        Employee employee = userService.getCurrentUser().getEmployee();
        if (employee == null) {
            throw new IllegalStateException("Current user is not associated with an employee.");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setStartDate(leaveRequestDTO.getStartDate());
        leaveRequest.setEndDate(leaveRequestDTO.getEndDate());
        leaveRequest.setStatus("PENDING"); // Default status
        leaveRequest.setEmployee(employee);

        return leaveRequestRepo.save(leaveRequest);
    }
    //for user to change date
    @Transactional
    public LeaveRequest updateLeaveRequestDates(Long leaveRequestId, LeaveRequestDTO leaveRequestDTO) {
        LeaveRequest leaveRequest = leaveRequestRepo.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + leaveRequestId));

        leaveRequest.setStartDate(leaveRequestDTO.getStartDate());
        leaveRequest.setEndDate(leaveRequestDTO.getEndDate());
        return leaveRequestRepo.save(leaveRequest);
    }
    //for admin
    @Transactional
    public LeaveRequest updateLeaveRequestStatus(Long leaveRequestId, String status) {
        LeaveRequest leaveRequest = leaveRequestRepo.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + leaveRequestId));

        leaveRequest.setStatus(status);
        return leaveRequestRepo.save(leaveRequest);
    }

    public Iterable<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepo.findAll();
    }

    public Iterable<LeaveRequest> getLeaveRequestsByEmployeeId(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        return leaveRequestRepo.findByEmployeeId(employeeId);
    }
}
