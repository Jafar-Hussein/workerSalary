package com.example.examen.service;

import com.example.examen.adminDTO.CheckOutInfoDTO;
import com.example.examen.dto.CheckOutAdjustmentDTO;
import com.example.examen.dto.CheckOutDTO;
import com.example.examen.model.CheckIn;
import com.example.examen.model.CheckOut;
import com.example.examen.model.User;
import com.example.examen.repo.CheckInRepo;
import com.example.examen.repo.CheckOutRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Skapar en konstruktor för att automatiskt injicera beroenden
public class CheckoutService {
    private static final Logger logger = LoggerFactory.getLogger(CheckoutService.class); // Logger för att logga meddelanden och fel
    private final CheckOutRepo checkOutRepo; // Repository för att hantera CheckOut-databasoperationer
    private final UserService userService; // Tjänst för att hantera användarrelaterade operationer
    private final EmployeeService employeeService; // Tjänst för att hantera anställda
    private final CheckInRepo checkInRepo; // Repository för att hantera CheckIn-databasoperationer

    @Autowired
    private final SalaryService salaryService; // Tjänst för att hantera löneberäkningar

    // Metod för att checka ut en användare
    @Transactional // Markerar att metoden ska vara transaktionell
    public void checkOut() {
        try {
            User user = userService.getCurrentUser(); // Hämta den aktuella inloggade användaren
            if (user != null && user.getEmployee() != null) {
                LocalDateTime now = LocalDateTime.now(); // Hämta aktuell tid

                // Kontrollera om en lönepost redan finns för den aktuella månaden, annars skapa en
                salaryService.findOrCreateSalaryByEmployeeIdAndMonth(user.getEmployee().getId(), YearMonth.from(now));

                // Skapa ett nytt CheckOut-objekt och spara det i databasen
                CheckOut checkOut = new CheckOut();
                checkOut.setEmployee(user.getEmployee());
                checkOut.setCheckOutDateTime(now); // Sätt utcheckningstiden till nuvarande tid
                checkOutRepo.save(checkOut); // Spara utcheckningen i databasen

                // Uppdatera arbetade timmar och räkna om lönen
                salaryService.updateWorkedHoursAndRecalculateSalary(user.getEmployee().getId());
            } else {
                throw new IllegalArgumentException("User or associated employee not found"); // Kasta fel om användaren eller den associerade anställda inte finns
            }
        } catch (IllegalArgumentException e) {
            logger.error("Validation error during checkout process: {}", e.getMessage()); // Logga valideringsfel
            throw e;  // Kasta om felet för att hanteras eller loggas av anroparen/controllern
        } catch (Exception e) {
            logger.error("Error during checkout process", e); // Logga eventuella andra fel
            throw new RuntimeException("Internal server error during checkout", e); // Kasta runtime-fel vid interna fel
        }
    }

    // Metod för att hämta alla anställdas utcheckningsinformation för adminpanelen
    public ResponseEntity<List<CheckOutInfoDTO>> getAllEmployeeCheckOuts() {
        List<CheckOut> checkOuts = checkOutRepo.findAll(); // Hämta alla utcheckningar från databasen

        // Konvertera varje CheckOut till ett CheckOutInfoDTO-objekt och returnera som en lista
        return ResponseEntity.ok(checkOuts.stream().map(checkOut -> {
            CheckOutInfoDTO dto = new CheckOutInfoDTO();
            String fullName = checkOut.getEmployee().getFirstName() + " " + checkOut.getEmployee().getLastName();
            dto.setEmployeeName(fullName); // Sätt anställdas fullständiga namn
            dto.setCheckOutTime(checkOut.getCheckOutDateTime()); // Sätt utcheckningstiden
            return dto;
        }).collect(Collectors.toList()));
    }

    // Metod för att hämta utcheckningar baserat på den nuvarande inloggade anställdas ID
    public List<CheckOutDTO> getCheckOutsByEmployeeId() {
        User user = userService.getCurrentUser(); // Hämta den aktuella användaren
        if (user != null && user.getEmployee() != null) {
            // Hämta alla utcheckningar för den anställda och konvertera dem till DTO-objekt
            return checkOutRepo.findAllByEmployeeId(user.getEmployee().getId()).stream()
                    .map(checkOut -> new CheckOutDTO(checkOut.getId(), checkOut.getCheckOutDateTime()))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("User or associated employee not found"); // Kasta fel om användaren eller den associerade anställda inte finns
        }
    }

    // Transaktionell metod för att justera utcheckningstiden
    @Transactional
    public CheckOut adjustCheckOutTime(Long checkOutId, CheckOutAdjustmentDTO adjustmentDTO) {
        User currentUser = userService.getCurrentUser(); // Hämta den aktuella användaren
        CheckOut checkOut = checkOutRepo.findById(checkOutId)
                .orElseThrow(() -> new IllegalArgumentException("Check-out not found with id: " + checkOutId)); // Hämta utcheckningen eller kasta fel

        // Kontrollera om användaren har behörighet att justera denna utcheckning
        if (currentUser.isCheckOutAdjustmentAllowed(checkOut)) {
            checkOut.setCheckOutDateTime(adjustmentDTO.getNewCheckOutDateTime()); // Uppdatera utcheckningstiden
            checkOutRepo.save(checkOut); // Spara ändringarna i databasen

            // Uppdatera arbetade timmar och räkna om lönen
            salaryService.updateWorkedHoursAndRecalculateSalary(checkOut.getEmployee().getId());
        } else {
            throw new IllegalStateException("Unauthorized to adjust this check-out"); // Kasta fel om användaren inte har behörighet
        }
        return checkOut;
    }
}
