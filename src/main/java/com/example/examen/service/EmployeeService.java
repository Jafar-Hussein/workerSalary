package com.example.examen.service;

import com.example.examen.adminDTO.AdminEmployeeDTO;
import com.example.examen.dto.EmployeeDTO;
import com.example.examen.model.Employee;
import com.example.examen.model.Salary;
import com.example.examen.model.User;
import com.example.examen.repo.EmployeeRepo;
import com.example.examen.repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepo employeeRepo;
    private final UserRepo userRepo;
    private final UserService userService;
    private final ModelMapper modelMapper;

    // for admin
    public ResponseEntity<?> saveEmployeeInfoByUsername(String username, EmployeeDTO employeeDTO) {
    // check if the user exists
        User user = userRepo.findByUsername(username).orElse(null);

        if (user != null) {
            Employee employee = convertDtoToEntity(employeeDTO);
            employee.setUser(user);
            employeeRepo.save(employee);
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
              return ResponseEntity.ok(employee);
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
  public ResponseEntity<?> updateEmployeeInfoByAdmin(Long id, AdminEmployeeDTO adminEmployeeDTO) {
      Employee employee = employeeRepo.findById(id)
              .orElseThrow(() -> new RuntimeException("Employee not found with id " + id));

      modelMapper.map(adminEmployeeDTO, employee);

      if (adminEmployeeDTO.getHourlyRate() != null) {
          Salary salary = employee.getSalary();
          if (salary == null) {
              salary = new Salary();
              salary.setEmployee(employee);
              // Other initializations for Salary if needed
          }
          salary.setHourlyRate(adminEmployeeDTO.getHourlyRate());
          // Handle updates or creation of other Salary fields if needed
      }

      Employee updatedEmployee = employeeRepo.save(employee);

      // Optionally convert the saved entity to AdminEmployeeDTO if needed
      AdminEmployeeDTO updatedAdminEmployeeDTO = modelMapper.map(updatedEmployee, AdminEmployeeDTO.class);

      return ResponseEntity.ok(updatedAdminEmployeeDTO);
  }

    // for admin
    public ResponseEntity<?> deleteEmployeeInfo(Long id) {
        if (!employeeRepo.existsById(id)) {
            return ResponseEntity.badRequest().body("Employee not found with id " + id);
        }
        employeeRepo.deleteById(id);
        return ResponseEntity.ok("Employee info deleted successfully");
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
