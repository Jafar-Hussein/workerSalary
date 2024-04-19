package com.example.examen.service;

import com.example.examen.dto.LeaveRequestDTO;
import com.example.examen.model.Employee;
import com.example.examen.model.LeaveRequest;
import com.example.examen.model.User;
import com.example.examen.repo.LeaveRequestRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LeaveRequestServiceTest {
    @Mock
    private LeaveRequestRepo leaveRequestRepo;
    @Mock
    private UserService userService;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    void createLeaveRequest_Success() {
        // Setup
        LeaveRequestDTO leaveRequestDTO = new LeaveRequestDTO();
        leaveRequestDTO.setStartDate(LocalDate.now());
        leaveRequestDTO.setEndDate(LocalDate.now().plusDays(5));

        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");

        User user = mock(User.class);
        when(user.getEmployee()).thenReturn(employee);
        when(userService.getCurrentUser()).thenReturn(user);
        when(leaveRequestRepo.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execution
        LeaveRequest result = leaveRequestService.createLeaveRequest(leaveRequestDTO);

        // Verification
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals(employee, result.getEmployee());
    }
    @Test
    void createLeaveRequest_UserHasNoEmployee_ThrowsException() {
        // Setup
        LeaveRequestDTO leaveRequestDTO = new LeaveRequestDTO();

        User user = mock(User.class);
        when(user.getEmployee()).thenReturn(null);
        when(userService.getCurrentUser()).thenReturn(user);

        // Execution & Verification
        assertThrows(IllegalStateException.class, () -> leaveRequestService.createLeaveRequest(leaveRequestDTO));
    }
    @Test
    void updateLeaveRequestDates_Success() {
        Long leaveRequestId = 1L;
        LeaveRequest existingRequest = new LeaveRequest();
        LeaveRequestDTO leaveRequestDTO = new LeaveRequestDTO();
        leaveRequestDTO.setStartDate(LocalDate.now());
        leaveRequestDTO.setEndDate(LocalDate.now().plusDays(10));

        when(leaveRequestRepo.findById(leaveRequestId)).thenReturn(Optional.of(existingRequest));
        when(leaveRequestRepo.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Execution
        LeaveRequest updatedRequest = leaveRequestService.updateLeaveRequestDates(leaveRequestId, leaveRequestDTO);

        // Verification
        assertEquals(leaveRequestDTO.getStartDate(), updatedRequest.getStartDate());
        assertEquals(leaveRequestDTO.getEndDate(), updatedRequest.getEndDate());
    }
    @Test
    void updateLeaveRequestDates_NonexistentId_ThrowsException() {
        Long leaveRequestId = 1L;
        LeaveRequestDTO leaveRequestDTO = new LeaveRequestDTO();

        when(leaveRequestRepo.findById(leaveRequestId)).thenReturn(Optional.empty());

        // Execution & Verification
        assertThrows(IllegalArgumentException.class, () -> leaveRequestService.updateLeaveRequestDates(leaveRequestId, leaveRequestDTO));
    }

}