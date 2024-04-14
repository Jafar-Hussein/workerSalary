package com.example.examen.service;

import com.example.examen.dto.LeaveRequestDTO;
import com.example.examen.model.Employee;
import com.example.examen.model.LeaveRequest;
import com.example.examen.repo.LeaveRequestRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        leaveRequest.setStatus("PENDING");
        leaveRequest.setEmployee(employee);

        // Set the employee's name in the leave request here
        leaveRequestDTO.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());

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

    public Iterable<LeaveRequestDTO> getAllLeaveRequests() {
        return leaveRequestRepo.findAll().stream()
                .map(leaveRequest -> {
                    LeaveRequestDTO leaveRequestDTO = new LeaveRequestDTO();
                    leaveRequestDTO.setId(leaveRequest.getId());
                    leaveRequestDTO.setStartDate(leaveRequest.getStartDate());
                    leaveRequestDTO.setEndDate(leaveRequest.getEndDate());
                    leaveRequestDTO.setStatus(leaveRequest.getStatus());
                    // Make sure the employee field is not null to avoid NullPointerException
                    if (leaveRequest.getEmployee() != null) {
                        leaveRequestDTO.setEmployeeName(leaveRequest.getEmployee().getFirstName() + " " + leaveRequest.getEmployee().getLastName());
                    }
                    return leaveRequestDTO;
                })
                .collect(Collectors.toList());
    }

    public Iterable<LeaveRequestDTO> getLeaveRequestsByEmployeeId(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }

        // Assuming findByEmployeeId returns List<LeaveRequest>
        List<LeaveRequest> leaveRequests = leaveRequestRepo.findByEmployeeId(employeeId);

        // Convert each LeaveRequest entity to LeaveRequestDTO
        List<LeaveRequestDTO> leaveRequestDTOs = leaveRequests.stream().map(leaveRequest -> {
            LeaveRequestDTO dto = new LeaveRequestDTO();
            dto.setId(leaveRequest.getId());
            dto.setStartDate(leaveRequest.getStartDate());
            dto.setEndDate(leaveRequest.getEndDate());
            dto.setStatus(leaveRequest.getStatus());
            // and so on for the other fields
            // ...
            return dto;
        }).collect(Collectors.toList());

        return leaveRequestDTOs; // This is now the correct type
    }

}
