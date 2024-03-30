package com.example.examen.controller;

import com.example.examen.adminDTO.AdminEmployeeDTO;
import com.example.examen.dto.EmployeeCreationDTO;
import com.example.examen.dto.EmployeeDTO;
import com.example.examen.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employee/")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping("add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> saveEmployeeInfo(@RequestBody EmployeeCreationDTO employeeCreationDTO){
        employeeService.saveEmployeeInfoByUsername(employeeCreationDTO.getUsername(),employeeCreationDTO.getEmployeeDTO());
        return ResponseEntity.ok("Employee info saved successfully");
    }

    @GetMapping("current")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getCurrentUserEmployeeInfo(){
        return employeeService.getCurrentUserEmployeeInfo();
    }

    @GetMapping("admin-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllEmployeesInfoAdmin(){
        return employeeService.getAllEmployeesInfoForAdmin();
    }

    @GetMapping("all")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getAllEmployeesInfo(){
        return employeeService.getAllEmployeesInfo();
    }

    @PutMapping("user-update")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> updateEmployeeInfo(@RequestBody EmployeeDTO employeeDTO){
        return employeeService.updateEmployeeInfo(employeeDTO);
    }
    @PutMapping("admin-update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateEmployeeInfoAdmin(@PathVariable Long id,@RequestBody AdminEmployeeDTO adminEmployeeDTO){
        return employeeService.updateEmployeeInfoByAdmin(id,adminEmployeeDTO);
    }

    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id){
        return employeeService.deleteEmployeeAndUser(id);
    }
}
