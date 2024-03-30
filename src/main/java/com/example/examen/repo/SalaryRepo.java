package com.example.examen.repo;

import com.example.examen.model.Employee;
import com.example.examen.model.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryRepo extends JpaRepository<Salary, Long> {
    Optional<Salary> findByEmployeeAndMonth(Employee employee, YearMonth month);
    Optional<Salary> findByEmployeeIdAndMonth(Long employeeId, YearMonth month);

    List<Salary> findAllByEmployee(Employee employee);


}
