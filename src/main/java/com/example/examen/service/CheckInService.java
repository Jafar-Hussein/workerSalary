package com.example.examen.service;

import com.example.examen.adminDTO.CheckInInfoDTO;
import com.example.examen.dto.CheckInAdjustmentDTO;
import com.example.examen.dto.CheckInDTO;
import com.example.examen.model.CheckIn;
import com.example.examen.model.Roles;
import com.example.examen.model.User;
import com.example.examen.repo.CheckInRepo;
import com.example.examen.repo.CheckOutRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Skapar en konstruktor för att automatiskt injicera beroenden
public class CheckInService {
    private final CheckInRepo checkInRepo; // Repository för att hantera CheckIn-databasoperationer
    private final UserService userService; // Tjänst för att hantera användarrelaterade operationer
    private final EmployeeService employeeService; // Tjänst för att hantera anställda
    private final CheckOutRepo checkOutRepo; // Repository för att hantera CheckOut-databasoperationer
    private final SalaryService salaryService; // Tjänst för att hantera löneberäkningar

    // Metod för att checka in en användare
    public void checkIn() {
        // Hämta den aktuella inloggade användaren
        User user = userService.getCurrentUser();
        if (user != null && user.getEmployee() != null) {
            LocalDateTime now = LocalDateTime.now(); // Hämta aktuell tid
            // Kontrollera om en lönepost redan finns för den aktuella månaden, annars skapa en
            salaryService.findOrCreateSalaryByEmployeeIdAndMonth(user.getEmployee().getId(), YearMonth.from(now));

            // Skapa ett nytt CheckIn-objekt och spara det i databasen
            CheckIn checkIn = new CheckIn();
            checkIn.setEmployee(user.getEmployee());
            checkIn.setCheckInDateTime(now); // Sätt incheckningstiden till nuvarande tid
            checkInRepo.save(checkIn); // Spara check-in i databasen
        } else {
            throw new IllegalArgumentException("User or associated employee not found"); // Kasta fel om användaren eller den associerade anställda inte finns
        }
    }

    // Metod för att hämta alla anställdas namn och incheckningskommentarer för adminpanelen
    public List<CheckInInfoDTO> getAllEmployeeCheckIns() {
        List<CheckIn> checkIns = checkInRepo.findAll(); // Hämta alla incheckningar från databasen

        // Konvertera varje CheckIn till ett CheckInInfoDTO-objekt och returnera som en lista
        return checkIns.stream().map(checkIn -> {
            CheckInInfoDTO dto = new CheckInInfoDTO();
            String fullName = checkIn.getEmployee().getFirstName() + " " + checkIn.getEmployee().getLastName();
            dto.setEmployeeName(fullName); // Sätt anställdas fullständiga namn
            dto.setCheckInTime(checkIn.getCheckInDateTime()); // Sätt incheckningstiden
            return dto;
        }).collect(Collectors.toList());
    }

    // Metod för att hämta incheckningar baserat på den nuvarande inloggade anställdas ID
    public List<CheckInDTO> getCheckInsByEmployeeId() {
        User user = userService.getCurrentUser(); // Hämta den aktuella användaren
        if (user != null && user.getEmployee() != null) {
            // Hämta alla incheckningar för den anställda och konvertera dem till DTO-objekt
            return checkInRepo.findAllByEmployeeId(user.getEmployee().getId()).stream()
                    .map(checkIn -> new CheckInDTO(checkIn.getId(), checkIn.getCheckInDateTime()))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("User or associated employee not found"); // Kasta fel om användaren eller den associerade anställda inte finns
        }
    }

    // Transaktionell metod för att justera incheckningstiden
    @Transactional
    public CheckIn adjustCheckInTime(Long checkInId, CheckInAdjustmentDTO adjustmentDTO) {
        User currentUser = userService.getCurrentUser(); // Hämta den aktuella användaren
        CheckIn checkIn = checkInRepo.findById(checkInId)
                .orElseThrow(() -> new IllegalArgumentException("CheckIn not found with id: " + checkInId)); // Hämta incheckningen eller kasta fel

        // Kontrollera om användaren har behörighet att justera denna incheckning
        if (currentUser.isCheckInAdjustmentAllowed(checkIn)) {
            checkIn.setCheckInDateTime(adjustmentDTO.getNewCheckInDateTime()); // Uppdatera incheckningstiden
            checkInRepo.save(checkIn); // Spara ändringarna i databasen

            // Uppdatera arbetade timmar och räkna om lönen utan att använda datum-parametrar
            salaryService.updateWorkedHoursAndRecalculateSalary(checkIn.getEmployee().getId());
        } else {
            throw new IllegalStateException("Unauthorized to adjust this check-in"); // Kasta fel om användaren inte har behörighet
        }
        return checkIn;
    }
}
