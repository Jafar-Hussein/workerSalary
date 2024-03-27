package com.example.examen.model;

import com.example.examen.converter.YearMonthStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;
@Entity
@Table(name = "salaries")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Salary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
    @Convert(converter = YearMonthStringConverter.class)
    private YearMonth month;
    private BigDecimal totalSalary;
    private BigDecimal hourlyRate;
}
