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
@RequiredArgsConstructor // Skapar en konstruktor för att automatiskt injicera beroenden
public class LeaveRequestService {
    private final LeaveRequestRepo leaveRequestRepo; // Repository för att hantera LeaveRequest-databasoperationer
    private final UserService userService; // Tjänst för att hantera användarrelaterade operationer

    // Metod för att skapa en ny ledighetsansökan
    @Transactional // Markerar att metoden ska vara transaktionell
    public LeaveRequest createLeaveRequest(LeaveRequestDTO leaveRequestDTO) {
        // Hämta den nuvarande användaren och dess associerade Employee
        Employee employee = userService.getCurrentUser().getEmployee();
        if (employee == null) {
            throw new IllegalStateException("Current user is not associated with an employee.");
        }

        // Skapa en ny LeaveRequest baserat på information från DTO
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setStartDate(leaveRequestDTO.getStartDate());
        leaveRequest.setEndDate(leaveRequestDTO.getEndDate());
        leaveRequest.setStatus("PENDING"); // Sätt status till "PENDING" som standard
        leaveRequest.setEmployee(employee);

        // Sätt anställdens namn i DTO:n
        leaveRequestDTO.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());

        return leaveRequestRepo.save(leaveRequest); // Spara LeaveRequest i databasen
    }

    // Metod för användare att ändra datum för sin ledighetsansökan
    @Transactional
    public LeaveRequest updateLeaveRequestDates(Long leaveRequestId, LeaveRequestDTO leaveRequestDTO) {
        // Hämta LeaveRequest genom ID eller kasta fel om den inte hittas
        LeaveRequest leaveRequest = leaveRequestRepo.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + leaveRequestId));

        // Uppdatera start- och slutdatum för ledighetsansökan
        leaveRequest.setStartDate(leaveRequestDTO.getStartDate());
        leaveRequest.setEndDate(leaveRequestDTO.getEndDate());

        return leaveRequestRepo.save(leaveRequest); // Spara uppdateringen i databasen
    }

    // Metod för admin att uppdatera status för en ledighetsansökan
    @Transactional
    public LeaveRequest updateLeaveRequestStatus(Long leaveRequestId, String status) {
        // Hämta LeaveRequest genom ID eller kasta fel om den inte hittas
        LeaveRequest leaveRequest = leaveRequestRepo.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + leaveRequestId));

        // Uppdatera status för ledighetsansökan
        leaveRequest.setStatus(status);

        return leaveRequestRepo.save(leaveRequest); // Spara uppdateringen i databasen
    }

    // Metod för att hämta alla ledighetsansökningar
    public Iterable<LeaveRequestDTO> getAllLeaveRequests() {
        // Hämta alla LeaveRequests från databasen och konvertera till DTO-objekt
        return leaveRequestRepo.findAll().stream()
                .map(leaveRequest -> {
                    LeaveRequestDTO leaveRequestDTO = new LeaveRequestDTO();
                    leaveRequestDTO.setId(leaveRequest.getId());
                    leaveRequestDTO.setStartDate(leaveRequest.getStartDate());
                    leaveRequestDTO.setEndDate(leaveRequest.getEndDate());
                    leaveRequestDTO.setStatus(leaveRequest.getStatus());

                    // Kontrollera att Employee inte är null för att undvika NullPointerException
                    if (leaveRequest.getEmployee() != null) {
                        leaveRequestDTO.setEmployeeName(leaveRequest.getEmployee().getFirstName() + " " + leaveRequest.getEmployee().getLastName());
                    }

                    return leaveRequestDTO;
                })
                .collect(Collectors.toList());
    }

    // Metod för att hämta ledighetsansökningar baserat på en anställds ID
    public Iterable<LeaveRequestDTO> getLeaveRequestsByEmployeeId(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }

        // Hämta alla LeaveRequests för en specifik anställd
        List<LeaveRequest> leaveRequests = leaveRequestRepo.findByEmployeeId(employeeId);

        // Konvertera varje LeaveRequest till LeaveRequestDTO
        List<LeaveRequestDTO> leaveRequestDTOs = leaveRequests.stream().map(leaveRequest -> {
            LeaveRequestDTO dto = new LeaveRequestDTO();
            dto.setId(leaveRequest.getId());
            dto.setStartDate(leaveRequest.getStartDate());
            dto.setEndDate(leaveRequest.getEndDate());
            dto.setStatus(leaveRequest.getStatus());
            // och andra fält om det behövs
            return dto;
        }).collect(Collectors.toList());

        return leaveRequestDTOs;
    }
}
