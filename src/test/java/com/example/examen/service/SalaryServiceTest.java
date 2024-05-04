package com.example.examen.service;

import com.example.examen.dto.SalaryDTO;
import com.example.examen.model.Employee;
import com.example.examen.model.Salary;
import com.example.examen.model.User;
import com.example.examen.repo.CheckInRepo;
import com.example.examen.repo.CheckOutRepo;
import com.example.examen.repo.EmployeeRepo;
import com.example.examen.repo.SalaryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SalaryServiceTest {

    @Mock
    private SalaryRepo salaryRepo;
    @Mock
    private EmployeeRepo employeeRepo;
    @Mock
    private CheckInRepo checkInRepo;
    @Mock
    private CheckOutRepo checkOutRepo;
    @Mock
    private UserService userService;

    @InjectMocks
    private SalaryService salaryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void setSalaryDetails_Success() {
        Long employeeId = 1L;
        SalaryDTO salaryDTO = new SalaryDTO();
        salaryDTO.setHourlyRate(new BigDecimal("30.00"));
        salaryDTO.setWorkedHours(160);
        salaryDTO.setTotalSalary(new BigDecimal("4800.00"));
        YearMonth currentMonth = YearMonth.now(); // Use the current month for the test
        salaryDTO.setMonth(currentMonth); // Ensure the month is set on the DTO

        Employee employee = new Employee();
        employee.setId(employeeId);
        Salary salary = new Salary(); // Create a Salary object to be returned by the mock
        salary.setEmployee(employee);
        salary.setMonth(currentMonth);

        // Mock the employee and salary finding behavior
        when(employeeRepo.findById(employeeId)).thenReturn(Optional.of(employee));
        when(salaryRepo.findByEmployeeIdAndMonth(employeeId, currentMonth)).thenReturn(Optional.empty()); // Simulate not finding the salary
        when(salaryRepo.save(any(Salary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Mock the findOrCreateSalaryByEmployeeIdAndMonth behavior
        when(salaryService.findOrCreateSalaryByEmployeeIdAndMonth(employeeId, currentMonth)).thenReturn(salary);

        // Call the method under test
        salaryService.setSalaryDetails(employeeId, salaryDTO);

        // Verify that the salary details are set as expected
        assertEquals(new BigDecimal("4800.00"), salary.getTotalSalary());
        assertEquals(160, salary.getWorkedHours());
        assertEquals(new BigDecimal("30.00"), salary.getHourlyRate());

        // Verify that the save method was called
        verify(salaryRepo).save(salary);
    }


    @Test
    void getCurrentMonthSalary_Success() {
        User user = new User();
        Employee employee = new Employee();
        employee.setId(1L);
        user.setEmployee(employee);
        Salary salary = new Salary();
        salary.setHourlyRate(new BigDecimal("30.00"));
        salary.setWorkedHours(160);
        salary.setTotalSalary(new BigDecimal("4800.00"));
        YearMonth currentMonth = YearMonth.now();

        when(userService.getCurrentUser()).thenReturn(user);
        when(employeeRepo.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryRepo.findByEmployeeAndMonth(employee, currentMonth)).thenReturn(Optional.of(salary));

        SalaryDTO result = salaryService.getCurrentMonthSalary();

        assertNotNull(result);
        assertEquals(new BigDecimal("4800.00"), result.getTotalSalary());
    }
    @Test
    void getPastSalaries_Success() {
        Long employeeId = 1L;
        Employee employee = new Employee();
        employee.setId(employeeId);
        List<Salary> salaries = List.of(
                new Salary(1L, employee, YearMonth.of(2023, 10), 160, new BigDecimal("3200.00"), new BigDecimal("20.00")),
                new Salary(2L, employee, YearMonth.of(2023, 9), 150, new BigDecimal("3750.00"), new BigDecimal("25.00"))
        );

        when(employeeRepo.findById(employeeId)).thenReturn(Optional.of(employee));
        when(salaryRepo.findAllByEmployee(employee)).thenReturn(salaries);

        List<SalaryDTO> result = salaryService.getPastSalaries(employeeId);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getMonth().isAfter(result.get(1).getMonth())); // Ensures correct order
        verify(salaryRepo).findAllByEmployee(employee);
    }

    @Test
    void updateHourlyRate_Success() {
        Long employeeId = 1L;
        BigDecimal newRate = new BigDecimal("35.00");
        Salary salary = new Salary();
        YearMonth currentMonth = YearMonth.now();

        when(salaryRepo.findByEmployeeIdAndMonth(employeeId, currentMonth)).thenReturn(Optional.of(salary));
        when(salaryRepo.save(any(Salary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<?> response = salaryService.updateHourlyRate(employeeId, newRate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Hourly rate updated successfully", response.getBody());
        assertEquals(new BigDecimal("35.00"), salary.getHourlyRate());
    }

}