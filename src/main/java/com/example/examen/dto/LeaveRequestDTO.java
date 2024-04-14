package com.example.examen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveRequestDTO {
    Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String employeeName;
}
