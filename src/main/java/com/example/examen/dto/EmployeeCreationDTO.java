package com.example.examen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeCreationDTO {
    private String username;
    private EmployeeDTO employeeDTO;
}
