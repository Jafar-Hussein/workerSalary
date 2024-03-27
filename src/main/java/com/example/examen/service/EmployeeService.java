package com.example.examen.service;

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

  // update employee info
//  public ResponseEntity<?> updateEmployeeInfo(Long id, EmployeeDTO employeeDTO) {
//      Employee employee = employeeRepo.findById(id)
//              .orElseThrow(() -> new RuntimeException("Employee not found with id " + id));
//
//      modelMapper.map(employeeDTO, employee);
//
//      // Check if salary information is included and needs to be updated
//      if (employeeDTO.getHourlyRate() != null && employee.getSalary() != null) {
//          Salary salary = employee.getSalary();
//          salary.setHourlyRate(employeeDTO.getHourlyRate());
//          // Handle other salary fields if necessary
//      }
//
//      Employee updatedEmployee = employeeRepo.save(employee);
//
//      // Optionally convert the saved entity to DTO if needed
//      EmployeeDTO updatedEmployeeDTO = modelMapper.map(updatedEmployee, EmployeeDTO.class);
//
//      return ResponseEntity.ok(updatedEmployeeDTO);
//  }
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
