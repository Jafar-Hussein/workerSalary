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

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepo employeeRepo;
    private final UserRepo userRepo;
    private final SalaryRepo salaryRepo;
    private final UserService userService;
    private final ModelMapper modelMapper;

    // for admin
    public ResponseEntity<?> saveEmployeeInfoByUsername(String username, EmployeeDTO employeeDTO) {
        User user = userRepo.findByUsername(username).orElse(null);

        if (user != null) {
            Employee employee = convertDtoToEntity(employeeDTO);
            employee.setUser(user); // Make sure the Employee knows about its User
            Employee savedEmployee = employeeRepo.save(employee);

            user.setEmployee(savedEmployee); // Update the User to reference the saved Employee
            userRepo.save(user); // Save the User entity to update the relationship in the database

            return ResponseEntity.ok("Employee info saved successfully");
        }
        return ResponseEntity.badRequest().body("User not found");
    }


  // get current user employee info
  public ResponseEntity<?> getCurrentUserEmployeeInfo() {
      User user = userService.getCurrentUser();
      if (user != null) {
          Employee employee = user.getEmployee();
          if (employee != null) {
              // Convert Employee entity to EmployeeDTO
              EmployeeDTO employeeDTO = modelMapper.map(employee, EmployeeDTO.class);
              return ResponseEntity.ok(employeeDTO);
          } else {
              return ResponseEntity.badRequest().body("Current user does not have associated employee information.");
          }
      }
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found or not logged in.");
  }


    // get all employees info
  public ResponseEntity<List<EmployeeDTO>> getAllEmployeesInfo() {
      // Fetch all employees from the database
      List<Employee> employees = employeeRepo.findAll();

      // Convert each employee entity to an EmployeeDTO
      List<EmployeeDTO> employeeDTOs = employees.stream()
              .map(employee -> modelMapper.map(employee, EmployeeDTO.class))
              .collect(Collectors.toList());

      // Return the list of EmployeeDTOs
      return ResponseEntity.ok(employeeDTOs);
  }
  // get all employees info for admin
    public ResponseEntity<List<AdminEmployeeDTO>> getAllEmployeesInfoForAdmin() {
        // Fetch all employees from the database
        List<Employee> employees = employeeRepo.findAll();

        // Convert each employee entity to an AdminEmployeeDTO
        List<AdminEmployeeDTO> adminEmployeeDTOs = employees.stream()
                .map(employee -> modelMapper.map(employee, AdminEmployeeDTO.class))
                .collect(Collectors.toList());

        // Return the list of AdminEmployeeDTOs
        return ResponseEntity.ok(adminEmployeeDTOs);
    }

  // employee update their own info
  public ResponseEntity<?> updateEmployeeInfo(EmployeeDTO employeeDTO) {
      User user = userService.getCurrentUser();
      if (user == null) {
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found or not logged in.");
      }

      Employee employee = user.getEmployee();
      if (employee == null) {
          return ResponseEntity.badRequest().body("Current user does not have associated employee information.");
      }

      modelMapper.map(employeeDTO, employee);
      Employee updatedEmployee = employeeRepo.save(employee);
      return ResponseEntity.ok(updatedEmployee);
  }
    // for admin
    // for admin
    public ResponseEntity<?> updateEmployeeInfoByAdmin(Long id, AdminEmployeeDTO adminEmployeeDTO) {
        Employee employee = employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id " + id));

        // Apply updates to the employee information from the AdminEmployeeDTO
        modelMapper.map(adminEmployeeDTO, employee);

        // Assuming you want to update the salary for the current month
        YearMonth currentMonth = YearMonth.now();
        Salary salary = employee.getSalaries().stream()
                .filter(s -> s.getMonth().equals(currentMonth))
                .findFirst()
                .orElse(new Salary());

        // Set or update salary details
        salary.setEmployee(employee);
        salary.setMonth(currentMonth); // This assumes you're updating the salary for the current month
        if (adminEmployeeDTO.getHourlyRate() != null) {
            salary.setHourlyRate(adminEmployeeDTO.getHourlyRate());
        }

        // If this is a new salary object, add it to the employee's list of salaries
        if (salary.getId() == null) {
            employee.getSalaries().add(salary);
        }

        salaryRepo.save(salary); // Save the updated or new salary record

        // Save the employee to update the relationship
        Employee updatedEmployee = employeeRepo.save(employee);

        // Map both Employee and Salary information back to AdminEmployeeDTO
        AdminEmployeeDTO updatedAdminEmployeeDTO = modelMapper.map(updatedEmployee, AdminEmployeeDTO.class);
        updatedAdminEmployeeDTO.setHourlyRate(salary.getHourlyRate()); // Ensure hourlyRate is set

        return ResponseEntity.ok("Employee info updated successfully");
    }


    // for admin
    public ResponseEntity<?> deleteEmployeeAndUser(Long employeeId) {
        // Attempt to find the employee by ID
        Optional<Employee> employeeOpt = employeeRepo.findById(employeeId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            User user = employee.getUser();
            if (user != null) {
                // Delete the employee. The employee entity should not have any non-nullable foreign keys preventing deletion.
                employeeRepo.delete(employee);
                // Delete the user associated with the employee.
                userRepo.delete(user);
                return ResponseEntity.ok().body("Employee and associated user deleted successfully.");
            } else {
                return ResponseEntity.badRequest().body("No user associated with this employee.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employee not found.");
        }
    }

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
