package com.example.examen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalaryDTO {
    private YearMonth month;
    private Integer workedHours;
    private BigDecimal totalSalary;
    private BigDecimal hourlyRate;
}
