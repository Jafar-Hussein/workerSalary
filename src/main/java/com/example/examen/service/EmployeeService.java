package com.example.examen.service;

import com.example.examen.adminDTO.AdminEmployeeDTO;
import com.example.examen.dto.EmployeeDTO;
import com.example.examen.model.Employee;
import com.example.examen.model.Salary;
import com.example.examen.model.User;
import com.example.examen.repo.EmployeeRepo;
import com.example.examen.repo.SalaryRepo;
import com.example.examen.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Skapar en konstruktor för att automatiskt injicera beroenden
public class EmployeeService {
    private final EmployeeRepo employeeRepo; // Repository för att hantera Employee-databasoperationer
    private final UserRepo userRepo; // Repository för att hantera User-databasoperationer
    private final SalaryRepo salaryRepo; // Repository för att hantera Salary-databasoperationer
    private final UserService userService; // Tjänst för att hantera användarrelaterade operationer
    private final ModelMapper modelMapper; // Används för att konvertera mellan entiteter och DTO-objekt

    // För admin: Spara anställds information baserat på användarnamn
    public ResponseEntity<?> saveEmployeeInfoByUsername(String username, EmployeeDTO employeeDTO) {
        User user = userRepo.findByUsername(username).orElse(null);

        if (user != null) {
            Employee employee = convertDtoToEntity(employeeDTO); // Konvertera DTO till entitet
            employee.setUser(user); // Koppla Employee till dess User
            Employee savedEmployee = employeeRepo.save(employee);

            user.setEmployee(savedEmployee); // Uppdatera User att referera till den sparade Employee
            userRepo.save(user); // Spara User-objektet i databasen

            return ResponseEntity.ok("Employee info saved successfully");
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    // Hämta nuvarande användares anställdinformation
    public ResponseEntity<?> getCurrentUserEmployeeInfo() {
        User user = userService.getCurrentUser(); // Hämta nuvarande inloggade användare
        if (user != null) {
            Employee employee = user.getEmployee(); // Hämta Employee kopplad till användaren
            if (employee != null) {
                // Konvertera Employee-entiteten till EmployeeDTO
                EmployeeDTO employeeDTO = modelMapper.map(employee, EmployeeDTO.class);
                return ResponseEntity.ok(employeeDTO); // Returnera EmployeeDTO
            } else {
                return ResponseEntity.badRequest().body("Current user does not have associated employee information.");
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found or not logged in.");
    }

    // Hämta alla anställdas information
    public ResponseEntity<List<EmployeeDTO>> getAllEmployeesInfo() {
        List<Employee> employees = employeeRepo.findAll(); // Hämta alla anställda från databasen

        // Konvertera varje Employee till EmployeeDTO och samla dem i en lista
        List<EmployeeDTO> employeeDTOs = employees.stream()
                .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());

        return ResponseEntity.ok(employeeDTOs); // Returnera listan med EmployeeDTOs
    }

    // För admin: Hämta alla anställdas information, inklusive löneuppgifter
    public ResponseEntity<List<AdminEmployeeDTO>> getAllEmployeesInfoForAdmin() {
        List<Employee> employees = employeeRepo.findAll(); // Hämta alla anställda från databasen

        // Konvertera varje Employee till AdminEmployeeDTO och hämta deras timlön om det finns
        List<AdminEmployeeDTO> adminEmployeeDTOs = employees.stream()
                .map(employee -> {
                    AdminEmployeeDTO dto = modelMapper.map(employee, AdminEmployeeDTO.class);
                    BigDecimal hourlyRate = employee.getSalaries().isEmpty() ? null : employee.getSalaries().get(0).getHourlyRate();
                    dto.setHourlyRate(hourlyRate); // Sätt timlönen om den finns
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(adminEmployeeDTOs); // Returnera listan med AdminEmployeeDTOs
    }

    // Anställd uppdaterar sin egen information
    public ResponseEntity<?> updateEmployeeInfo(EmployeeDTO employeeDTO) {
        User user = userService.getCurrentUser(); // Hämta den aktuella inloggade användaren
        Employee existingEmployee = user.getEmployee();
        if (existingEmployee == null) {
            throw new RuntimeException("No employee information found for current user");
        }

        // Modellmappning exkluderar id-fältet vid uppdatering
        modelMapper.typeMap(EmployeeDTO.class, Employee.class)
                .addMappings(mapper -> mapper.skip(Employee::setId));

        modelMapper.map(employeeDTO, existingEmployee); // Utför mappningen, exkludera id

        Employee updatedEmployee = employeeRepo.save(existingEmployee); // Spara den uppdaterade anställda
        return ResponseEntity.ok(updatedEmployee); // Returnera uppdaterad Employee
    }

    // För admin: Uppdatera en anställds information och löneuppgifter
    public ResponseEntity<?> updateEmployeeInfoByAdmin(Long id, AdminEmployeeDTO adminEmployeeDTO) {
        Employee employee = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id " + id));

        // Mappa AdminEmployeeDTO till Employee-objektet
        modelMapper.map(adminEmployeeDTO, employee);

        // Uppdatera lönen för den aktuella månaden
        YearMonth currentMonth = YearMonth.now();
        Salary salary = employee.getSalaries().stream()
                .filter(s -> s.getMonth().equals(currentMonth))
                .findFirst()
                .orElse(new Salary());

        // Sätt eller uppdatera lönedetaljer
        salary.setEmployee(employee);
        salary.setMonth(currentMonth);
        if (adminEmployeeDTO.getHourlyRate() != null) {
            salary.setHourlyRate(adminEmployeeDTO.getHourlyRate());
        }

        // Om det är en ny lönepost, lägg till den i anställdas lönelista
        if (salary.getId() == null) {
            employee.getSalaries().add(salary);
        }

        salaryRepo.save(salary); // Spara den uppdaterade eller nya löneposten

        Employee updatedEmployee = employeeRepo.save(employee); // Spara den uppdaterade anställda

        AdminEmployeeDTO updatedAdminEmployeeDTO = modelMapper.map(updatedEmployee, AdminEmployeeDTO.class);
        updatedAdminEmployeeDTO.setHourlyRate(salary.getHourlyRate()); // Säkerställ att timlönen sätts

        return ResponseEntity.ok("Employee info updated successfully");
    }

    // För admin: Ta bort en anställd och den associerade användaren
    public ResponseEntity<?> deleteEmployeeAndUser(Long employeeId) {
        Optional<Employee> employeeOpt = employeeRepo.findById(employeeId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            User user = employee.getUser();
            if (user != null) {
                employeeRepo.delete(employee); // Ta bort anställd
                userRepo.delete(user); // Ta bort den associerade användaren
                return ResponseEntity.ok().body("Employee and associated user deleted successfully.");
            } else {
                return ResponseEntity.badRequest().body("No user associated with this employee.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found.");
        }
    }

    // Konvertera EmployeeDTO till Employee-entitet
    private Employee convertDtoToEntity(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setAddress(employeeDTO.getAddress());
        employee.setCity(employeeDTO.getCity());
        employee.setJobTitle(employeeDTO.getJobTitle());
        employee.setEmail(employeeDTO.getEmail());
        employee.setPhone(employeeDTO.getPhone());
        return employee;
    }
}
